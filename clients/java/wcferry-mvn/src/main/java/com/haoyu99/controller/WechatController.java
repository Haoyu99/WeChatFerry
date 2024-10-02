package com.haoyu99.controller;

import com.haoyu99.Client;
import com.haoyu99.dto.FileMessageDTO;
import com.haoyu99.dto.TextMessageDTO;
import com.haoyu99.entity.ContactInfo;
import com.haoyu99.entity.ContactType;
import com.haoyu99.entity.GroupContactInfo;
import com.haoyu99.entity.PersonalInfo;
import com.haoyu99.service.WechatService;
import com.haoyu99.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
@RequestMapping("/api/wechat")
public class WechatController {
    @Resource
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
     * 获取群聊的全部成员
     * @author haoyu99
     * @date 2024/10/2 1:39
     * @param: roomId
     * @return com.haoyu99.utils.Response<java.util.List < com.haoyu99.entity.GroupContactInfo>>
     */

    @GetMapping("/room/members")
    public Response<List<GroupContactInfo>> getRoomMember(@RequestParam("roomId") String roomId){
        return Response.success(wechatService.getChatRoomMembers(roomId));
    }


    /**
     * 单发消息
     * @author haoyu99
     * @date 2024/9/28 20:23
     * @param: contactInfo
     * @return com.haoyu99.utils.Response<java.lang.String>
     */

    @PostMapping("/send/text/contact")
    public Response<String> sendTextToContact(@RequestBody TextMessageDTO textMessageDTO){
        int code = wechatService.sendTextMsg(textMessageDTO.getMessage(),
                textMessageDTO.getReceiver(), new ArrayList<>());
        if(code != -1){
            return Response.success("发送成功");
        }else {
            return Response.failure("发送失败");
        }
    }

    /**
     * 群发消息
     * @author haoyu99
     * @date 2024/9/28 20:23
     * @param: contactInfo
     * @return com.haoyu99.utils.Response<java.lang.String>
     */

    @PostMapping("/send/text/contacts")
    public Response<String> sendTextToContacts(@RequestBody List<TextMessageDTO> textMessageDTOs){
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

    @PostMapping("/send/text/chatRoom")
    public Response<String> sendTextToRoom(@RequestBody TextMessageDTO textMessageDTO){
        List<String> aters = textMessageDTO.getAters();
        String chatRoomId = textMessageDTO.getReceiver();
        List<GroupContactInfo> chatRoomMembers = wechatService.getChatRoomMembers(chatRoomId);

        String atString = "@" + String.join(" @", aters);
        String message = textMessageDTO.getMessage();
        wechatService.sendTextMsg(atString + message, chatRoomId, aters);
        return Response.success("发送成功");
    }

    /**
     * 发送除TEXT类型外的Message
     * @author haoyu99
     * @date 2024/10/2 12:24
     * @param:
     * @return com.haoyu99.utils.Response<java.lang.String>
     */

    @PostMapping("/send/file/contact")
    public Response<String> sendFile(@RequestBody FileMessageDTO fileMessageDTO){
        int code = wechatService.sendFile(fileMessageDTO.getMessageTypeCode(), fileMessageDTO.getFilePath(),
                fileMessageDTO.getReceiver());
        if(code != -1){
            return Response.success("发送成功");
        }else {
            return Response.failure("发送失败");
        }
    }

    @PostMapping("/receive/open")
    public Response<String> openMessageReceiver(){
        boolean isOpen = wechatService.openMessageReceiver();
        if(isOpen){
            return Response.success("开启消息接收成功");
        }else {
            return Response.failure("开启消息接收失败");
        }
    }

    @PostMapping("/receive/close")
    public Response<String> closeMessageReceiver(){
        boolean isClose = wechatService.closeMessageReceiver();
        if(isClose){
            return Response.success("关闭消息接收成功");
        }else {
            return Response.failure("关闭消息接收失败");
        }
    }
}
