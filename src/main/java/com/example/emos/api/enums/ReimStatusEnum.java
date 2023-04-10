package com.example.emos.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReimStatusEnum {

    UNAPPLY(0, "待提交"),
    WAIT(1, "审批中"),
    UN_APPROVAL(2, "已拒绝"),
    APPROVAL(3,"审批通过"),
    ARCHIVED(4,"已归档"),
    ;

    private Integer code;
    private String desc;

    public static ReimStatusEnum getEumByCode(Integer code){
        if(code == null) return null;

        for(ReimStatusEnum statusEnum: ReimStatusEnum.values()) {
            if(statusEnum.getCode() == code) {
                return statusEnum;
            }
        }
        return null;
    }

}
