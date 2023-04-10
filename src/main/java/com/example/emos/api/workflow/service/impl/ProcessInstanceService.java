package com.example.emos.api.workflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.example.emos.api.common.util.R;
import com.example.emos.api.common.util.UserUtils;
import com.example.emos.api.db.dao.TbLeaveDao;
import com.example.emos.api.db.dao.TbMeetingDao;
import com.example.emos.api.db.dao.TbReimDao;
import com.example.emos.api.db.pojo.TbBusinessStatus;
import com.example.emos.api.db.pojo.TbProcessConfig;
import com.example.emos.api.db.req.StartREQ;
import com.example.emos.api.enums.LeaveStatusEnum;
import com.example.emos.api.enums.MeetingStatusEnum;
import com.example.emos.api.enums.ReimStatusEnum;
import com.example.emos.api.service.BusinessStatusService;
import com.example.emos.api.service.ProcessConfigService;
import com.example.emos.api.service.UserService;
import com.example.emos.api.workflow.service.IProcessInstanceService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProcessInstanceService extends ActivitiService implements IProcessInstanceService {

    @Autowired
    ProcessConfigService processConfigService;

    @Autowired
    BusinessStatusService businessStatusService;

    @Autowired
    UserService userService;

    @Autowired
    private TbLeaveDao leaveDao;

    @Autowired
    private TbReimDao reimDao;

    @Autowired
    private TbMeetingDao meetingDao;

    @Override
    public int startProcess(StartREQ req) {
        // 1. 通过业务路由名获取流程配置信息：流程定义key和表单组件名（查询历史审批记录需要）
        TbProcessConfig processConfig =
                processConfigService.getByBusinessRoute(req.getBusinessRoute());

        // 2. 表单组件名设置到流程变量中，后面查询历史审批记录需要
        Map<String, Object> variables = new HashMap<>(); // 前端已经传递了当前申请信息｛entity: {业务申请数据}}
        variables.put("formName", processConfig.getFormName());
        variables.put("userId", StpUtil.getLoginIdAsInt());
        HashMap map = new HashMap();
        map.put("id", req.getBusinessKey());
        map.put("userId", StpUtil.getLoginIdAsInt());

        //请假流程实例
        HashMap leave = leaveDao.searchLeaveById(map);
        if (leave != null) {
            String dayCnt = (String) leave.get("dayCnt");
            variables.put("duration", Double.parseDouble(dayCnt));
        }

        // 3. 启动流程实例（提交申请）
        HashMap user = userService.searchById(UserUtils.getUserId());
        String name = (String) user.get("name");

        Authentication.setAuthenticatedUserId(name);

        ProcessInstance pi =
                runtimeService.startProcessInstanceByKey(processConfig.getProcessKey(),
                        req.getBusinessKey(), variables);

        // 将流程定义名称 作为 流程实例名称
//        runtimeService.setProcessInstanceName(pi.getProcessInstanceId(), pi.getProcessDefinitionName());

        // 4. 设置任务办理人
        List<String> assignees = req.getAssignees();
        if (!CollectionUtil.isEmpty(assignees)) {
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
            for (Task task : taskList) {
                if (assignees.size() == 1) {
                    // 如果只能一个办理人，则直接设置为办理人
                    taskService.setAssignee(task.getId(), assignees.get(0));
                } else {
                    // 多个办理人，则设置为候选人
                    for (String assignee : assignees) {
                        taskService.addCandidateUser(task.getId(), assignee);
                    }
                }
            }
        }
        String processKey = processConfig.getProcessKey();

        if (processKey.equals("LeaveProcess")) {
            HashMap hashMap = new HashMap();
            hashMap.put("businessKey", req.getBusinessKey());
            HashMap leaveEntity = leaveDao.searchLeaveByBusinessKey(hashMap);
            leaveEntity.put("status", LeaveStatusEnum.WAIT.getCode());
            leaveEntity.put("instanceId", pi.getProcessInstanceId());
            return leaveDao.updateLeaveInstanceId(leaveEntity);
        } else if (processKey.equals("ConferenceApp")) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uuid", req.getBusinessKey());
            hashMap.put("status", MeetingStatusEnum.WAIT.getCode());
            hashMap.put("instanceId", pi.getProcessInstanceId());
            return meetingDao.updateMeetingInstanceId(hashMap);
        } else if (processKey.equals("reimLeave")) {
            HashMap reim = new HashMap();
            reim.put("id", req.getBusinessKey());
            reim.put("status", ReimStatusEnum.WAIT.getCode());
            reim.put("instanceId",pi.getProcessInstanceId());
            return reimDao.updateReimInstanceId(reim);
        }
        return 0;

        /*TbBusinessStatus update = businessStatusService.update(businessStatus);
        if (update != null)
            return 1;
*/


    }

    @Override
    public int cancel(String businessKey, String procInstId, String message) {
        // 1. 删除当前流程实例
        runtimeService.deleteProcessInstance(procInstId,
                UserUtils.getUsername() + " 主动撤回了当前申请：" + message);

        // 2. 删除历史记录
        historyService.deleteHistoricProcessInstance(procInstId);
        historyService.deleteHistoricTaskInstance(procInstId);

        TbBusinessStatus businessStatus = new TbBusinessStatus();
        businessStatus.setBusinessKey(businessKey);
        businessStatus.setUpdateDate(new Date());

        businessStatus.setProcessInstanceId("");
        // 3. 更新业务状态
        TbBusinessStatus result = businessStatusService.update(businessStatus);

        return result != null ? 1 : 0;
    }




}
