package com.example.emos.api.db.pojo;

import java.util.Date;
import java.io.Serializable;

/**
 * 流程定义配置表(TbProcessConfig)实体类
 *
 * @author makejava
 * @since 2022-04-12 23:30:21
 */
public class TbProcessConfig implements Serializable {
    private static final long serialVersionUID = 239086061480860484L;
    /**
     * 主键id
     */
    private String id;
    /**
     * 流程定义KEY
     */
    private String processKey;
    /**
     * 业务申请路由名
     */
    private String businessRoute;
    /**
     * 关联表单组件名
     */
    private String formName;
    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 更新时间
     */
    private Date updateDate;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcessKey() {
        return processKey;
    }

    public void setProcessKey(String processKey) {
        this.processKey = processKey;
    }

    public String getBusinessRoute() {
        return businessRoute;
    }

    public void setBusinessRoute(String businessRoute) {
        this.businessRoute = businessRoute;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
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

