package com.haoyu99.service.impl;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.haoyu99.config.WcferryProperties;
import com.haoyu99.constant.SQLConstant;
import com.haoyu99.entity.*;
import com.haoyu99.proto.Wcf;
import com.haoyu99.service.SDK;
import com.haoyu99.service.WechatService;
import com.haoyu99.utils.Convertor;
import com.sun.jna.Native;
import io.sisu.nng.Socket;
import io.sisu.nng.pair.Pair1Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;


/***
 * @title WechatServiceImpl
 * @description 核心能力接口实现类
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 17:33
 **/
@Slf4j
@Service
public class WechatServiceImpl implements WechatService, SQLConstant {
    @Resource
    private WcferryProperties properties;

    private static final int BUFFER_SIZE = 16 * 1024 * 1024; // 16M
    // RPC Socket
    private Socket cmdSocket;
    // Message Socket
    private Socket msgSocket;

    private final Map<Integer, Function<byte[], Object>> SQL_TYPES = new HashMap<>();

    private final Map<MessageType, Wcf.Functions> messageTypeFunctionsMap = new HashMap<>();

    private Map<Integer, String> receiveMessageTypeMap = new HashMap<>();

    private final String URL = "tcp://%s:%s";

    private LinkedBlockingQueue<Wcf.WxMsg> messageQueue;

    private volatile boolean isReceivingMsg = false;

    private volatile Thread listenThread = null;

    private ExecutorService executorService;

    private volatile boolean isProcessingMessages = false;

    private final Object socketLock = new Object();


    @Autowired
    private Convertor convertor;

    @Value("${wcferry.socket-host}")
    private String host;
    @Value("${wcferry.socket-port}")
    private int port;
    @Value("${wcferry.dll-path}")
    private String dllPath;
    @Value("${wcferry.message-queue-size}")
    private int messageQueueMaxSize;

    @PostConstruct
    public void init() {
        SDK INSTANCE = Native.load(dllPath, SDK.class);
        int status = INSTANCE.WxInitSDK(false, port);
        if (status != 0) {
            log.error("启动 RPC 失败: {}", status);
            System.exit(-1);
        }
        connectRPC(String.format(URL, host, port), INSTANCE);
        // 初始化map
        this.initMap();
        // 初始化消息队列
        messageQueue = new LinkedBlockingQueue<>(messageQueueMaxSize);
        // 初始化线程池
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        log.info("WechatService init success!");
        String selfWechatId = getSelfWechatId();
        log.info("获取到登录微信id{}", selfWechatId);
    }

    public void initMap(){
        // 初始化SQL_TYPES 根据类型执行不同的Func
        SQL_TYPES.put(1, bytes -> ByteBuffer.wrap(bytes).getInt());
        SQL_TYPES.put(2, bytes -> ByteBuffer.wrap(bytes).getFloat());
        SQL_TYPES.put(3, bytes -> new String(bytes, StandardCharsets.UTF_8));
        SQL_TYPES.put(4, bytes -> bytes);
        SQL_TYPES.put(5, bytes -> null);
        // 初始化messageTypeFunctionsMap
        messageTypeFunctionsMap.put(MessageType.IMAGE, Wcf.Functions.FUNC_SEND_IMG);
        messageTypeFunctionsMap.put(MessageType.FILE, Wcf.Functions.FUNC_SEND_FILE);
        messageTypeFunctionsMap.put(MessageType.XML, Wcf.Functions.FUNC_SEND_XML);
        messageTypeFunctionsMap.put(MessageType.EMOTION, Wcf.Functions.FUNC_SEND_EMOTION);
        // 初始化receiveMessageTypeMap
        receiveMessageTypeMap = getMessageTypesMap();
    }


    public void connectRPC(String url, SDK INSTANCE) {
        int maxRetries = 5; // 设置最大重试次数
        int retries = 0; // 初始化重试计数器
        try {
            cmdSocket = new Pair1Socket();
            cmdSocket.dial(url);
            while (!isLogin() && retries < maxRetries) {
                waitMs(1000);
                retries++;
            }
            if(!isLogin()) {
                log.error("达到重试最大状态，请检查微信登录状态");
            }
        } catch (Exception e) {
            log.error("连接 RPC 失败: ", e);
            System.exit(-1);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("关闭...");
            closeMessageReceiver();
            INSTANCE.WxDestroySDK();
        }));
    }

    private Wcf.Response sendCmd(Wcf.Request req) {
        try {
            ByteBuffer bb = ByteBuffer.wrap(req.toByteArray());
            cmdSocket.send(bb);
            ByteBuffer ret = ByteBuffer.allocate(BUFFER_SIZE);
            long size = cmdSocket.receive(ret, true);
            return Wcf.Response.parseFrom(Arrays.copyOfRange(ret.array(), 0, (int)size));
        } catch (Exception e) {
            log.error("RPC调用失败: ", e);
            return null;
        }
    }

    @Override
    public boolean isLogin() {
        Wcf.Request req = Wcf.Request.newBuilder().setFuncValue(Wcf.Functions.FUNC_IS_LOGIN_VALUE).build();
        Wcf.Response rsp = sendCmd(req);
        if (rsp != null) {
            return rsp.getStatus() == 1;
        }
        return false;
    }

    @Override
    public List<ContactInfo> getContactInfos() {
        Wcf.Request request = Wcf.Request.newBuilder().setFuncValue(Wcf.Functions.FUNC_GET_CONTACTS_VALUE).build();
        Wcf.Response response = sendCmd(request);
        if(response != null){
            List<Wcf.RpcContact> contactsList = response.getContacts().getContactsList();
            List<ContactInfo> contactInfos = convertor.convertListFromRpcContactList(contactsList);
            log.info("获取到当前微信所有联系人:{}", contactInfos);
            return contactInfos;
        }
        return null;
    }

    @Override
    public PersonalInfo getPersonalInfo() {
        Wcf.Request req = Wcf.Request.newBuilder().setFuncValue(Wcf.Functions.FUNC_GET_USER_INFO_VALUE).build();
        Wcf.Response rsp = sendCmd(req);
        if (rsp != null) {
            Wcf.UserInfo userInfo = rsp.getUi();
            PersonalInfo personalInfo = convertor.convertToSelfInfo(userInfo);
            log.info("获取到当前微信个人资料:{}", personalInfo);
            return personalInfo;
        }
        return null;
    }

    @Override
    public int sendTextMsg(String message, String receiver, List<String> atUsers) {
        Wcf.TextMsg textMsg = Wcf.TextMsg.newBuilder()
                .setMsg(message)
                .setReceiver(receiver)
                .setAters(String.join(",", atUsers))
                .build();
        Wcf.Request request = Wcf.Request.newBuilder()
                .setFuncValue(Wcf.Functions.FUNC_SEND_TXT_VALUE)
                .setTxt(textMsg)
                .build();
        log.debug("sendText: {}", bytesToHex(request.toByteArray()));
        Wcf.Response response = sendCmd(request);
        int ret = -1;
        if (response != null) {
            ret = response.getStatus();
        }

        return ret;
    }

    /**
     * 在指定数据库执行SQL 返回List<>代表每一行 Map<>代表每一的每一个Field
     * @author haoyu99
     * @date 2024/10/2 1:03
     * @param: db
     * @param: sql
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     */

    public List<Map<String, Object>> querySql(String db, String sql) {
        List<Map<String, Object>> result = new ArrayList<>();

        Wcf.DbQuery dbQuery = Wcf.DbQuery.newBuilder()
                .setDb(db)
                .setSql(sql)
                .build();

        Wcf.Request request = Wcf.Request.newBuilder()
                .setFuncValue(Wcf.Functions.FUNC_EXEC_DB_QUERY_VALUE)
                .setQuery(dbQuery)
                .build();

        Wcf.Response response = sendCmd(request);
        if (response != null && response.hasRows()) {
            // 获取指定的行
            List<Wcf.DbRow> rows = response.getRows().getRowsList();
            for (Wcf.DbRow row : rows) {
                Map<String, Object> rowMap = new HashMap<>();
                // 遍历每一列
                for (Wcf.DbField field : row.getFieldsList()) {
                    ByteString content = field.getContent();
                    String column = field.getColumn();
                    int type = field.getType();
                    // 根据每一列的类型转换
                    Function<byte[], Object> converter = SQL_TYPES.get(type);
                    if (converter != null) {
                        rowMap.put(column, converter.apply(content.toByteArray()));
                    } else {
                        log.warn("未知的SQL类型: {}", type);
                        rowMap.put(column, content.toByteArray());
                    }
                }
                result.add(rowMap);
            }
        }
        log.debug("在{}执行SQL:{}, 结果为: {}", db, sql, result);
        return result;
    }

    @Override
    public int sendFile(int code, String filePath, String receiver) {
        MessageType messageType = MessageType.getMessageTypeFromCode(code);
        if(!messageTypeFunctionsMap.containsKey(messageType)){
            log.error("不支持的MessageType:{}", messageType);
            return -1;
        }
        Wcf.Functions functions = messageTypeFunctionsMap.get(messageType);
        Wcf.PathMsg pathMsg = Wcf.PathMsg.newBuilder().setPath(filePath).setReceiver(receiver).build();
        Wcf.Request req = Wcf.Request.newBuilder().setFuncValue(functions.getNumber()).setFile(pathMsg).build();
        log.debug("sendFile: {}", bytesToHex(req.toByteArray()));
        Wcf.Response rsp = sendCmd(req);
        int ret = -1;
        if (rsp != null) {
            ret = rsp.getStatus();
        }
        return ret;
    }

    @Override
    public List<GroupContactInfo> getChatRoomMembers(String roomId) {
        String sql = String.format(queryRoomDataFromChatRoom, roomId);
        List<Map<String, Object>> maps = querySql(MicroMsgDB, sql);
        if(maps.isEmpty()){
            log.error("roomId:{}, 查询结果为空", roomId);
            return Collections.emptyList();
        }
        Map<String, Object> stringObjectMap = maps.get(0);
        byte[] roomDataBytes = (byte[]) stringObjectMap.get("RoomData");

        try {
            Wcf.RoomData roomData = Wcf.RoomData.parseFrom(roomDataBytes);
            ArrayList<GroupContactInfo> groupContactInfoList = new ArrayList<>();
            for (Wcf.RoomData.RoomMember member : roomData.getMembersList()) {
                GroupContactInfo userInfo = new GroupContactInfo();
                userInfo.setWxid(member.getWxid());
                userInfo.setName(member.getName());
                userInfo.setState(member.getState());
                groupContactInfoList.add(userInfo);
            }
            System.out.println(groupContactInfoList);
            return groupContactInfoList;
        } catch (InvalidProtocolBufferException e) {
            log.error("解析RoomData失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean openMessageReceiver() {
        if (isReceivingMsg) {
            log.info("已开启消息接收");
            return true;
        }
        Wcf.Request req = Wcf.Request.newBuilder().setFuncValue(Wcf.Functions.FUNC_ENABLE_RECV_TXT_VALUE).build();
        Wcf.Response rsp = sendCmd(req);
        if (rsp == null) {
            log.error("启动消息接收失败");
            return false;
        }
        isReceivingMsg = true;
        //TODO: 处理开启消息监听失败的情况。
        listenMessage();
        //TODO: 处理开启消息消费失败的情况
        MessageConsumer();
        log.info("开启消息接收");
        return true;
    }

    @Override
    public boolean closeMessageReceiver() {
        if (!isReceivingMsg) {
            log.info("消息接收已关闭");
            return true;
        }
        // 关闭监听线程
        if (listenThread != null && listenThread.isAlive()) {
            try {
                listenThread.interrupt();
                listenThread.join(3000); // 等待最多3秒
            } catch (InterruptedException e) {
                log.error("等待消息接收线程结束失败", e);
                Thread.currentThread().interrupt();
            }
        }
        // 关闭线程池
        if (executorService != null) {
            executorService.shutdown(); // 温和地关闭线程池
            try {
                // 等待正在进行的任务完成
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    // 如果等待超时，强制关闭
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
                log.error("关闭线程池时被中断", e);
            }
        }
        // 安全地关闭socket
        synchronized (socketLock) {
            if (msgSocket != null) {
                try {
                    msgSocket.close();
                } catch (Exception e) {
                    log.error("关闭消息 RPC 失败", e);
                } finally {
                    msgSocket = null;
                }
            }
        }
        // 处理剩余的消息
        processRemainingMessages();
        isProcessingMessages = false;

        listenThread = null;
        executorService = null;
        Wcf.Request req = Wcf.Request.newBuilder().setFuncValue(Wcf.Functions.FUNC_DISABLE_RECV_TXT_VALUE).build();
        Wcf.Response rsp = sendCmd(req);
        if(rsp != null){
            if (rsp.getStatus() == 0) {
                isReceivingMsg = false;
                log.info("消息接收关闭");
                return true;
            }
        }
        return false;
    }

    public String getSelfWechatId() {
        Wcf.Request req = Wcf.Request.newBuilder().setFuncValue(Wcf.Functions.FUNC_GET_SELF_WXID_VALUE).build();
        Wcf.Response rsp = sendCmd(req);
        if (rsp != null) {
            return rsp.getStr();
        }
        return "";
    }

    public Map<Integer, String> getMessageTypesMap() {
        Wcf.Request req = Wcf.Request.newBuilder().setFuncValue(Wcf.Functions.FUNC_GET_MSG_TYPES_VALUE).build();
        Wcf.Response rsp = sendCmd(req);
        if (rsp != null) {
            return rsp.getTypes().getTypesMap();
        }
        return Wcf.MsgTypes.newBuilder().build().getTypesMap();
    }

    private void listenMessage(){
        if (listenThread != null && listenThread.isAlive()) {
            return;
        }

        String messageUrl = String.format(URL, host, port + 1);
        listenThread = new Thread(() -> {
            try {
                msgSocket = new Pair1Socket();
                msgSocket.dial(messageUrl);
                msgSocket.setReceiveTimeout(5000); // 5 秒超时
            } catch (Exception e) {
                log.error("创建消息 RPC 失败", e);
                return;
            }

            ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);
            while (isReceivingMsg) {
                try {
                    long size = msgSocket.receive(bb, true);
                    if (size > 0) {
                        Wcf.WxMsg wxMsg = Wcf.Response.parseFrom(
                                Arrays.copyOfRange(bb.array(), 0, (int) size)).getWxmsg();
                        messageQueue.put(wxMsg);
                    }
                    bb.clear(); // 清空缓冲区，准备下一次接收
                } catch (Exception e) {
                    if (isReceivingMsg) {
//                        忽略这个异常 不影响使用
//                        log.info("接收信息超时", e);
                    }
                }
            }
        });
        listenThread.start();
    }

    private void MessageConsumer() {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        isProcessingMessages = true;
        executorService.submit(() -> {
            while (isProcessingMessages && !Thread.currentThread().isInterrupted()) {
                try {
                    Wcf.WxMsg wxMsg = messageQueue.poll(5, TimeUnit.SECONDS);
                    if (wxMsg != null) {
                        WeChatMessage weChatMessage = Convertor.convertWxMsgToWeChatMessage(wxMsg);
                       //TODO: 消息入库
                        System.out.println(weChatMessage);
                        if(weChatMessage.getType() == MessageType.TEXT){
                            //TODO：方法路由器
                        }else {
                            log.info("暂时不支持的数据格式");
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("消息消费者被中断", e);
                } catch (Exception e) {
                    log.error("处理消息时发生错误", e);
                }
            }
        });
    }

    private void processRemainingMessages() {
        List<Wcf.WxMsg> remainingMessages = new ArrayList<>();
        messageQueue.drainTo(remainingMessages);
        if (!remainingMessages.isEmpty()) {
            log.info("处理剩余的 {} 条消息", remainingMessages.size());
            for (Wcf.WxMsg msg : remainingMessages) {
                try {
                    System.out.println(msg);;
                } catch (Exception e) {
                    log.error("处理剩余消息时发生错误", e);
                }
            }
        }
    }

    public void waitMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
