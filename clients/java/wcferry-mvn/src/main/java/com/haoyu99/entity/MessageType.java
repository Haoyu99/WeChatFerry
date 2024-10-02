package com.haoyu99.entity;


/***
 * @title MessageType
 * @description 消息类型枚举类
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 14:28
 **/
public enum MessageType {
    TEXT(0),
    // FILE同时支持IMAGE
    FILE(1),
    IMAGE(2),
    XML(3),
    EMOTION(4);

    private final int code;
    MessageType(int code) {
        this.code = code;
    }
    // 获取数字代码的方法
    public int getCode() {
        return code;
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
