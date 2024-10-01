package com.haoyu99.controller;

import com.haoyu99.dto.TextMessageDTO;
import com.haoyu99.entity.ContactInfo;
import com.haoyu99.entity.ContactType;
import com.haoyu99.entity.PersonalInfo;
import com.haoyu99.service.WechatService;
import com.haoyu99.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    /**
     * 获取所有联系人，按照类型分类
     * @author haoyu99
     * @date 2024/10/1 20:57
     * @param:
     * @return com.haoyu99.utils.Response<java.util.Map< com.haoyu99.entity.ContactType,
            * java.util.List < com.haoyu99.entity.ContactInfo>>>
     */

    @GetMapping("/contacts")
    public Response<Map<ContactType, List<ContactInfo>>> getContacts(){
        List<ContactInfo> contacts = wechatService.getContactInfos();
        Map<ContactType, List<ContactInfo>> result = contacts.stream()
                .collect(Collectors.groupingBy(ContactInfo::getType));
        return Response.success(result);
    }
    /**
     * 获取个人信息
     * @author haoyu99
     * @date 2024/10/1 19:26
     * @param:
     * @return com.haoyu99.utils.Response<com.haoyu99.entity.PersonalInfo>
     */

    @GetMapping("/selfInfo")
    public Response<PersonalInfo> getSelfInfo(){
        return Response.success(wechatService.getPersonalInfo());
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
