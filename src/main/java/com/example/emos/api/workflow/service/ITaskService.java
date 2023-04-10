package com.example.emos.api.workflow.service;

import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.SearchTaskByPageForm;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

public interface ITaskService {
    PageUtils findWaitTask(SearchTaskByPageForm searchTaskByPageForm);
    PageUtils findCompleteTask(SearchTaskByPageForm searchTaskByPageForm);


    HashMap searchApprovalContent(HashMap param);

    void getHistoryProcessImage(String instanceId, HttpServletResponse response);

    R completeTask(HashMap param);

    R archiveTask(HashMap param);
}
