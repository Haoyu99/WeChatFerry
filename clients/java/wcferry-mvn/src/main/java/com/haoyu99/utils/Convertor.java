package com.haoyu99.utils;

import com.haoyu99.entity.*;
import com.haoyu99.proto.Wcf;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/***
 * @title Convertor
 * @description 类型转换器
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 18:47
 **/
@Component
public class Convertor {
    public ContactInfo convertFromRpcContact(Wcf.RpcContact rpcContact) {
        ContactInfo contactInfo = new ContactInfo();
        BeanUtils.copyProperties(rpcContact, contactInfo);
        contactInfo.setType(this.getContactType(contactInfo.getWxid()));
        return contactInfo;
    }
    public List<ContactInfo> convertListFromRpcContactList(List<Wcf.RpcContact> rpcContactList) {
        return rpcContactList.stream()
                .map(this::convertFromRpcContact)
                .collect(Collectors.toList());
    }
    public PersonalInfo convertToSelfInfo(Wcf.UserInfo rpcUserInfo) {
        PersonalInfo personalInfo = new PersonalInfo();
        BeanUtils.copyProperties(rpcUserInfo, personalInfo);
        return personalInfo;
    }
    private ContactType getContactType(String wxid) {
        if (wxid.startsWith("gh_")) {
            return ContactType.OFFICIAL_ACCOUNT;
        } else if (wxid.endsWith("@chatroom")) {
            return ContactType.CHATROOM;
        } else if (wxid.endsWith("@openim")) {
            return ContactType.ENTERPRISE;
        } else if (isOfficialUtil(wxid)) {
            return ContactType.OFFICIAL_UTIL;
        } else {
            return ContactType.INDIVIDUAL;
        }
    }
    private boolean isOfficialUtil(String wxid) {
        return wxid.equals("fmessage") || wxid.equals("medianote") || wxid.equals("filehelper");
    }

    public static WeChatMessage convertWxMsgToWeChatMessage(Wcf.WxMsg wxMsg){
        WeChatMessage weChatMessage = new WeChatMessage();
        BeanUtils.copyProperties(wxMsg, weChatMessage);
        weChatMessage.setType(MessageType.getMessageTypeFromCode(wxMsg.getType()));
        return weChatMessage;
    }

}
