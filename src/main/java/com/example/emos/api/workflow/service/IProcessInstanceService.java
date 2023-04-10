package com.example.emos.api.workflow.service;


import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.InsertMeetingForm;
import com.example.emos.api.db.pojo.TbMeeting;
import com.example.emos.api.db.req.StartREQ;


public interface IProcessInstanceService {

    /**
     * 提交申请启动实例
     * @param req
     * @return
     */
    int startProcess(StartREQ req);
    /**
     * 撤回申请
     * @param businessKey
     * @param procInstId
     * @param message
     * @return
     */
    int cancel(String businessKey, String procInstId, String message);

    /**
     * 通过流程实例id查询流程变量formName值
     * @param procInstId
     * @return
     *//*
    R getFormNameByProcInstId(String procInstId);

    R getHistoryInfoList(String procInstId);

    *//**
     * 查询流程实例审批历史流程图
     * @param prodInstId
     * @param response
     *//*
    void getHistoryProcessImage(String prodInstId, HttpServletResponse response);

    *//**
     * 查询正在运行的流程实例
     * @param req
     * @return
     *//*
    R getProcInstRunning(ProcInstREQ req);

    *//**
     * 查询已结束的流程实例
     * @param req
     * @return
     *//*
    R getProcInstFinish(ProcInstREQ req);

    *//**
     * 删除流程实例与历史记录
     * @param procInstId
     * @return
     *//*
    R deleteProcInstAndHistory(String procInstId);*/
}
