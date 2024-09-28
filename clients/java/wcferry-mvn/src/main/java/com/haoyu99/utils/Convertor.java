package com.haoyu99.utils;

import com.haoyu99.entity.ContactInfo;
import com.haoyu99.entity.SelfInfo;
import com.haoyu99.proto.Wcf;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/***
 * @title Convertor
 * @description 类型转换器
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 18:47
 **/
public class Convertor {
    public static ContactInfo convertFromRpcContact(Wcf.RpcContact rpcContact) {
        ContactInfo contactInfo = new ContactInfo();
        BeanUtils.copyProperties(rpcContact, contactInfo);
        return contactInfo;
    }
    public static List<ContactInfo> convertListFromRpcContactList(List<Wcf.RpcContact> rpcContactList) {
        return rpcContactList.stream()
                .map(Convertor::convertFromRpcContact)
                .collect(Collectors.toList());
    }
    public static SelfInfo convertToSelfInfo(Wcf.UserInfo rpcUserInfo) {
        SelfInfo selfInfo = new SelfInfo();
        BeanUtils.copyProperties(rpcUserInfo, selfInfo);
        return selfInfo;
    }

}
