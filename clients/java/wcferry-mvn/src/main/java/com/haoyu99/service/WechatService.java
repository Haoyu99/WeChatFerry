package com.haoyu99.service;

import com.haoyu99.entity.ContactInfo;
import com.haoyu99.entity.GroupContactInfo;
import com.haoyu99.entity.PersonalInfo;

import java.util.List;

/***
 * @title WechatService
 * @description WechatFerry 核心能力接口
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 12:19
 **/
public interface WechatService {


    /**
     * 判断当前客户端是否登录微信
     * @author haoyu99
     * @date 2024/9/28 12:45
     * @return boolean
     */

    boolean isLogin();

    /**
     * 获取所有联系人信息
     * @author haoyu99
     * @date 2024/9/28 13:03
     * @param:
     * @return java.util.List<com.haoyu99.entity.Contact>
     */

    List<ContactInfo> getContactInfos();
    /**
     * 获取当前登录微信的个人信息
     * @author haoyu99
     * @date 2024/9/28 13:21
     * @param:
     * @return com.haoyu99.entity.PersonalInfo
     */

    PersonalInfo getPersonalInfo();

    /**
     * 发送文本消息
     * @author haoyu99
     * @date 2024/9/28 13:35
     * @param: message 消息体
     * @param: receiver 接收者可以是room
     * @param: atUsers 被@的用户
     * @return int
     */

    int sendTextMsg(String message, String receiver, List<String> atUsers);
    /**
     *
     * @author haoyu99
     * @date 2024/9/28 14:36
     * @param: messageType  EMOTION IMAGE FILE XML
     * @param: filePath 文件本地路径（可能支持URL）
     * @param: receiver 接收者 可以是个人也可以是room
     * @return int
     */

    int sendFile(int code, String filePath, String receiver);


    /**
     * 获取群成员
     * @author haoyu99
     * @date 2024/10/2 1:18
     * @param: roomId
     * @return java.util.List<com.haoyu99.entity.GroupContactInfo>
     */

    List<GroupContactInfo> getChatRoomMembers(String roomId);

    /**
     * 开启消息接收并选择消息处理器进行消费
     * @author haoyu99
     * @date 2024/9/28 17:31
     * @param: messageProcessor
     * @return boolean
     */

    boolean openMessageReceiver();

    /**
     * 关闭消息处接收
     * @author haoyu99
     * @date 2024/9/28 17:32
     * @param:
     * @return boolean
     */

    boolean closeMessageReceiver();



}
