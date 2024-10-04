package com.haoyu99.entity;


import lombok.Getter;

/***
 * @title MessageType
 * @description 消息类型枚举类
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 14:28
 **/
@Getter
public enum MessageType {
    MOMENTS(0),              // 朋友圈
    TEXT(1),                 // 文本
    IMAGE(3),                // 图片
    AUDIO(34),               // 语音
    FRIEND_CONFIRMATION(37), // 好友确认
    PROFILE_CARD(42),        // 名片
    VIDEO(43),               // 视频
    EMOTION(47),             // 表情包
    LOCATION(48),            // 位置
    // FILE同时支持IMAGE,
    FILE(2),                 // 发送消息中的FILE
    XML(49),                 // 视频号、共享实时位置、文件、转账、链接、消息引用、公众号文章 需要进一步判断，
    SYSTEM(10000);           // 红包、系统消息（拍一拍，共享位置结束等 添加好友提醒

    // 获取数字代码的方法
    private final int code;
    MessageType(int code) {
        this.code = code;
    }

    public static MessageType getMessageTypeFromCode(int code) {
        for (MessageType type : MessageType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid message type code: " + code);
    }
}
