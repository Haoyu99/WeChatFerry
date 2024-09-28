package com.haoyu99.service.processor;

import com.haoyu99.proto.Wcf;

/***
 * @title MessageProcessor
 * @description 消息处理器
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 17:21
 **/
public interface MessageProcessor {
    void process(Wcf.WxMsg wxMsg);
}
