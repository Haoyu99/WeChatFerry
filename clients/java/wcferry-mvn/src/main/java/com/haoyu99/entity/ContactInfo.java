package com.haoyu99.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/***
 * @title ContactInfo
 * @description Contact信息实体类
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 13:00
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactInfo {
    private String wxid;     // 微信 id
    private String code;     // 微信号
    private String remark;   // 备注
    private String name;     // 微信昵称
    private String country;  // 国家
    private String province; // 省/州
    private String city;     // 城市
    private int gender;  // 性别
}
