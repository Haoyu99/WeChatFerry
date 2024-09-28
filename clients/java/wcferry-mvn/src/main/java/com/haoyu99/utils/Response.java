package com.haoyu99.utils;

import lombok.Data;

/***
 * @title Response
 * @description 返回通用类
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/9/28 20:15
 **/
@Data
public class Response<T> {
    private int code;
    private String message;
    private T data;
    public Response(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    public static <T> Response<T> success(String message) {
        return new Response<T>(200, message, null);
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(200, "Success", data);
    }

    public static <T> Response<T> failure(String message) {
        return new Response<>(500, message, null);
    }
}
