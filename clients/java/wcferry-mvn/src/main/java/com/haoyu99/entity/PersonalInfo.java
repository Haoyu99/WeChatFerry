package com.haoyu99.entity;

import lombok.*;

/***
 * @title PersonalInfo
 * @description 登录微信个人信息
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/10/1 19:19
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonalInfo extends BaseUserInfo{
    private String mobile; // 手机号
    private String home; // 文件/图片等父路径

    @Override
    public String toString() {
        return "PersonalInfo{" +
                "mobile='" + mobile + '\'' +
                ", home='" + home + '\'' +
                ", wxid='" + wxid + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
