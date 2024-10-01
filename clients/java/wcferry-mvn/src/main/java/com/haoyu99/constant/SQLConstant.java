package com.haoyu99.constant;

/***
 * @title SQLConstant
 * @description
 * @author haoyu99
 * @version 1.0.0
 * @create 2024/10/2 1:01
 **/
public interface SQLConstant {
    // 用到的DBName
    String MicroMsgDB = "MicroMsg.db";


    // 用到的SQL
    String queryRoomDataFromChatRoom = "SELECT RoomData FROM ChatRoom WHERE ChatRoomName = '%s'";
}
