package com.example.emos.api.workflow.controller;

import com.example.emos.api.common.util.R;
import com.example.emos.api.db.req.StartREQ;
import com.example.emos.api.workflow.service.IProcessInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "工作流实例")
@Slf4j
@RestController
@RequestMapping("/instance")
public class ProcessInstanceController {

    @Autowired
    private IProcessInstanceService processInstanceService;

    @Operation(description = "提交申请，启动流程实例")
    @PostMapping("/start")
    public R start(@RequestBody StartREQ req) {
        int rows = processInstanceService.startProcess(req);
        return R.ok().put("rows", rows);
    }




}
