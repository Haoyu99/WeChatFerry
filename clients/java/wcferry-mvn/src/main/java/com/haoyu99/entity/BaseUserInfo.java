package com.haoyu99.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/***
 * @title BaseUserInfo
 * @description 用户信息基类
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/10/1 19:17
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseUserInfo {
    protected String wxid; // 微信ID
    protected String name; // 昵称
}
