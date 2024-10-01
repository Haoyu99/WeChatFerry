package com.haoyu99.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/***
 * @title ContactInfo
 * @description Contact联系人信息
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 13:00
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactInfo extends BaseUserInfo {
    private String code;     // 微信号
    private String remark;   // 备注
    private String country;  // 国家
    private String province; // 省/州
    private String city;     // 城市
    private int gender;  // 性别
    private ContactType type; //联系人类型

    @Override
    public String toString() {
        return "ContactInfo{" +
                "wxid='" + wxid + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", remark='" + remark + '\'' +
                ", country='" + country + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", gender=" + gender +
                ", type=" + type +
                '}';
    }
}
