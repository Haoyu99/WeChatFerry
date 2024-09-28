package com.haoyu99.entity;

import lombok.*;

/***
 * @title SelfInfo
 * @description 登录微信个人信息
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 13:13
 **/
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelfInfo extends ContactInfo{
    private String mobile;
    // 存放文件的路径
    private String home;
}
