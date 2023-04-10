package com.example.emos.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.ApprovalTaskForm;
import com.example.emos.api.controller.form.ArchiveTaskForm;
import com.example.emos.api.controller.form.SearchApprovalContentForm;
import com.example.emos.api.controller.form.SearchTaskByPageForm;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.workflow.service.ITaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;

@RestController
@RequestMapping("/approval")
@Tag(name = "ApprovalController", description = "任务审批Web接口")
@Slf4j
public class ApprovalController {


    @Autowired
    private ITaskService taskService;

    @PostMapping("/searchTaskByPage")
    @Operation(summary = "查询分页任务列表")
    @SaCheckPermission(value = {"WORKFLOW:APPROVAL", "FILE:ARCHIVE"}, mode = SaMode.OR)
    public R searchTaskByPage(@Valid @RequestBody SearchTaskByPageForm form) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        PageUtils pageUtils = taskService.findWaitTask(form);
//        PageUtils pageUtils = approvalService.searchTaskByPage(param);
        return R.ok().put("page", pageUtils);
    }

    @PostMapping("/searchHistoryTaskByPage")
    @Operation(summary = "查询已办任务列表")
    @SaCheckPermission(value = {"WORKFLOW:APPROVAL", "FILE:ARCHIVE"}, mode = SaMode.OR)
    public R searchHistoryTaskByPage(@Valid @RequestBody SearchTaskByPageForm form) {
        PageUtils pageUtils = taskService.findCompleteTask(form);
        return R.ok().put("page", pageUtils);
    }


    @PostMapping("/searchApprovalContent")
    @Operation(summary = "查询任务详情")
    @SaCheckPermission(value = {"WORKFLOW:APPROVAL", "FILE:ARCHIVE"}, mode = SaMode.OR)
    public R searchApprovalContent(@Valid @RequestBody SearchApprovalContentForm form) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        HashMap content = taskService.searchApprovalContent(param);
        return R.ok().put("content", content);
    }

    @GetMapping("/searchApprovalBpmn")
    @Operation(summary = "获取BPMN图形")
    @SaCheckPermission(value = {"WORKFLOW:APPROVAL", "FILE:ARCHIVE"}, mode = SaMode.OR)
    public void searchApprovalBpmn(String instanceId, HttpServletResponse response) {
        if (StrUtil.isBlankIfStr(instanceId)) {
            throw new EmosException("instanceId不能为空");
        }
        taskService.getHistoryProcessImage(instanceId, response);
    }

    @PostMapping("/approvalTask")
    @Operation(summary = "审批任务")
    @SaCheckPermission(value = {"WORKFLOW:APPROVAL"}, mode = SaMode.OR)
    public R approvalTask(@Valid @RequestBody ApprovalTaskForm form) {
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);

        return taskService.completeTask(param);
    }

    @PostMapping("/archiveTask")
    @Operation(summary = "归档任务")
    @SaCheckPermission(value = {"FILE:ARCHIVE"})
    public R archiveTask(@Valid @RequestBody ArchiveTaskForm form) {
        if (!JSONUtil.isJsonArray(form.getFiles())) {
            return R.error("files不是JSON数组");
        }
        HashMap param = new HashMap() {{
            put("taskId", form.getTaskId());
            put("files", form.getFiles());
            put("userId", StpUtil.getLoginIdAsInt());
            put("businessKey", form.getBusinessKey());
            put("processName", form.getProcessName());
        }};
        return taskService.archiveTask(param);
    }
}
