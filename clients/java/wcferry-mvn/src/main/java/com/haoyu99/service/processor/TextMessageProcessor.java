package com.haoyu99.service.processor;

import com.haoyu99.proto.Wcf;
import lombok.extern.slf4j.Slf4j;

/***
 * @title TextMessageProcessor
 * @description 文本消息处理器
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 17:25
 **/
@Slf4j
public class TextMessageProcessor implements MessageProcessor{
    @Override
    public void process(Wcf.WxMsg wxMsg) {
        log.info(wxMsg.toString());
    }
}
