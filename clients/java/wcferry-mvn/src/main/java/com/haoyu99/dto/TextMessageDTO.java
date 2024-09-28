package com.haoyu99.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/***
 * @title TextMessageDTO
 * @description
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 20:28
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextMessageDTO {
    private String message;
    private String receiver;
    List<String> aters;
}
