package com.haoyu99.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/***
 * @title FileMessageDTO
 * @description
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/10/2 12:19
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMessageDTO {
    private int messageTypeCode;
    private String filePath;
    private String receiver;
}
