package com.haoyu99.controller;

import com.haoyu99.dto.TextMessageDTO;
import com.haoyu99.entity.ContactInfo;
import com.haoyu99.entity.SelfInfo;
import com.haoyu99.service.WechatService;
import com.haoyu99.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***
 * @title WechatController
 * @description
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 18:33
 **/
@Slf4j
@RestController
@RequestMapping("/api")
public class WechatController {
    @Autowired
    private WechatService wechatService;

    @GetMapping("/selfId")
    public Response<String> getSelfWechatId(){
        return Response.success(wechatService.getSelfWechatId());
    }

    @GetMapping("/contacts")
    public Response<List<ContactInfo>> getContacts(){
        return Response.success(wechatService.getContactInfos());
    }

    @GetMapping("/selfInfo")
    public Response<SelfInfo> getSelfInfo(){
        return Response.success(wechatService.getSelfInfo());
    }
    /**
     * 单发消息
     * @author haoyu99
     * @date 2024/9/28 20:23
     * @param: contactInfo
     * @return com.haoyu99.utils.Response<java.lang.String>
     */

    @PostMapping("/sendTxt/contact")
    public Response<String> sendTxtToContact(@RequestBody TextMessageDTO textMessageDTO){
        wechatService.sendTextMsg(textMessageDTO.getMessage(),
                textMessageDTO.getReceiver(), new ArrayList<>());
        return Response.success("发送成功");
    }

    /**
     * 群发消息
     * @author haoyu99
     * @date 2024/9/28 20:23
     * @param: contactInfo
     * @return com.haoyu99.utils.Response<java.lang.String>
     */

    @PostMapping("/sendTxt/contacts")
    public Response<String> sendTxtToContacts(@RequestBody List<TextMessageDTO> textMessageDTOs){
        textMessageDTOs.forEach(textMessageDTO ->
                wechatService.sendTextMsg(textMessageDTO.getMessage(),
                        textMessageDTO.getReceiver(), new ArrayList<>()));
        return Response.success("发送成功");
    }
    /**
     * 群内发消息
     * @author haoyu99
     * @date 2024/9/28 21:15
     * @param: textMessageDTO
     * @return com.haoyu99.utils.Response<java.lang.String>
     */

    @PostMapping("/sendTxt/room")
    public Response<String> sendTxtToRoom(@RequestBody TextMessageDTO textMessageDTO){
//        TODO: @几个人 拼字符串
        String message = textMessageDTO.getMessage()+"@niu@zhang";

        wechatService.sendTextMsg(message, textMessageDTO.getReceiver(), textMessageDTO.getAters());
        return Response.success("发送成功");
    }
}
