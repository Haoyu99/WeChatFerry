package com.haoyu99.service.impl;

import com.haoyu99.entity.ContactInfo;
import com.haoyu99.entity.MessageType;
import com.haoyu99.entity.PersonalInfo;
import com.haoyu99.proto.Wcf;
import com.haoyu99.service.SDK;
import com.haoyu99.service.WechatService;
import com.haoyu99.service.processor.MessageProcessor;
import com.haoyu99.utils.Convertor;
import com.sun.jna.Native;
import io.sisu.nng.Socket;
import io.sisu.nng.pair.Pair1Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/***
 * @title WechatServiceImpl
 * @description 核心能力接口实现类
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 17:33
 **/
@Slf4j
@Service
public class WechatServiceImpl implements WechatService {

    private static final int BUFFER_SIZE = 16 * 1024 * 1024; // 16M
    // RPC Socket
    private Socket cmdSocket;
    // Message Socket
    private Socket msgSocket;

    private boolean isReceivingMessage = false;;

    private BlockingQueue<Wcf.WxMsg> msgQueue = new LinkedBlockingQueue<>();

    @Autowired
    private Convertor convertor;

    @Value("${wcferry.socket-host}")
    private String host;
    @Value("${wcferry.socket-port}")
    private int port;
    @Value("${wcferry.dll-path}")
    private String dllPath;

    @PostConstruct
    public void init() {
        SDK INSTANCE = Native.load(dllPath, SDK.class);
        int status = INSTANCE.WxInitSDK(false, port);
        if (status != 0) {
            log.error("启动 RPC 失败: {}", status);
            System.exit(-1);
        }
        String CMDURL = "tcp://%s:%s";
        connectRPC(String.format(CMDURL, host, port), INSTANCE);
        log.info("WechatService init success!");
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
    public String getSelfWechatId() {
        Wcf.Request req = Wcf.Request.newBuilder().setFuncValue(Wcf.Functions.FUNC_GET_SELF_WXID_VALUE).build();
        Wcf.Response rsp = sendCmd(req);
        if (rsp != null) {
            String selfWechatId = rsp.getStr();
            log.info("获取到个人微信号:{}", selfWechatId);
            return selfWechatId;
        }
        return "";
    }

    @Override
    public Map<Integer, String> getMsgTypes() {
        Wcf.Request req = Wcf.Request.newBuilder().setFuncValue(Wcf.Functions.FUNC_GET_MSG_TYPES_VALUE).build();
        Wcf.Response rsp = sendCmd(req);
        if (rsp != null) {
            return rsp.getTypes().getTypesMap();
        }

        return Wcf.MsgTypes.newBuilder().build().getTypesMap();    }

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


    @Override
    public int sendFile(MessageType messageType, String filePath, String receiver) {
        return 0;
    }

    @Override
    public Map<String, String> getChatRoomMembers() {
        return null;
    }

    @Override
    public boolean openMessageReceiver(MessageProcessor messageProcessor) {
        return false;
    }

    @Override
    public boolean closeMessageReceiver() {
        return false;
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
