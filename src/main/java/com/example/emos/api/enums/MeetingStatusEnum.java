package com.example.emos.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MeetingStatusEnum {

    /**
     * 会议审批：0:待提交、1.审批中，2.审批未通过，3.审批通过，4.会议进行中，5.会议结束
     */
    UNAPPLY(0, "待提交"), WAIT(1, "审批中"),
    APPROVAL_FAILED(2, "审批未通过"), APPROVALED(3, "审批通过"),
    MEETING_PROCESS(4, "会议进行中"),
    MEETING_END(5, "会议结束");
    private Integer code;
    private String desc;

    public static MeetingStatusEnum getEumByCode(Integer code) {
        if (code == null) return null;

        for (MeetingStatusEnum statusEnum : MeetingStatusEnum.values()) {
            if (statusEnum.getCode() == code) {
                return statusEnum;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
