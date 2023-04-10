package com.example.emos.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeaveStatusEnum {

    UNAPPLY(0, "待提交"), WAIT(1, "请假中"),
    UN_APPROVAL(2, "不同意"),APPROVAL(3,"已同意");

    private Integer code;
    private String desc;

    public static LeaveStatusEnum getEumByCode(Integer code){
        if(code == null) return null;

        for(LeaveStatusEnum statusEnum: LeaveStatusEnum.values()) {
            if(statusEnum.getCode() == code) {
                return statusEnum;
            }
        }
        return null;
    }

}
