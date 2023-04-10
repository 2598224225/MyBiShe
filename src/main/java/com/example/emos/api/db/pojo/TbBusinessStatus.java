package com.example.emos.api.db.pojo;

import java.util.Date;
import java.io.Serializable;

/**
 * 业务状态实体表(TbBusinessStatus)实体类
 *
 * @author makejava
 * @since 2022-04-12 23:32:38
 */
public class TbBusinessStatus implements Serializable {
    private static final long serialVersionUID = 936525707900459030L;
    /**
     * 业务ID
     */
    private String businessKey;
    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 更新时间
     */
    private Date updateDate;


    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }


    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

}

