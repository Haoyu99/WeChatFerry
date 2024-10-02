package com.haoyu99.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

/***
 * @title GroupContactInfo
 * @description 群内联系人信息
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/10/2 0:13
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupContactInfo extends BaseUserInfo{
    // 暂时不知道这个字段什么意思
    private int state;

    @Override
    public String toString() {
        return "GroupContactInfo{" +
                "state=" + state +
                ", wxid='" + wxid + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
