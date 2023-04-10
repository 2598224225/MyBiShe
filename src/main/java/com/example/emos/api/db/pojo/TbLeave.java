package com.example.emos.api.db.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class TbLeave {
    private String id;
    private Integer userId;
    private String reason;
    private String start;
    private String end;
    private String status;
    private String days;
    private Byte type;
    private Date createTime;
    private String fileUrl;
}
