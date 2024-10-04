package com.haoyu99.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeChatMessage {
    private boolean isGroup; // 是否群消息
    private long id; // 消息id
    private MessageType type; // 消息类型
    private int ts; // 时间戳
    private String roomId; // 群id（如果是群消息的话）
    private String content; // 消息内容
    private String sender; // 消息发送者
    private String sign; // Sign
    private String thumb; // 缩略图
    private String extra; // 附加内容
    private String xml; // 消息xml
}
