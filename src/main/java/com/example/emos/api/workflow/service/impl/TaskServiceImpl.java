package com.example.emos.api.workflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.example.emos.api.common.util.DateUtils;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.common.util.UserUtils;
import com.example.emos.api.controller.form.SearchTaskByPageForm;
import com.example.emos.api.db.dao.*;
import com.example.emos.api.db.pojo.TbBusinessStatus;
import com.example.emos.api.db.properties.FileUrl;
import com.example.emos.api.enums.LeaveStatusEnum;
import com.example.emos.api.enums.MeetingStatusEnum;
import com.example.emos.api.enums.ReimStatusEnum;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.workflow.image.CustomProcessDiagramGenerator;
import com.example.emos.api.workflow.service.ITaskService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.*;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl extends ActivitiService implements ITaskService {

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private TbMeetingDao meetingDao;

    @Autowired
    private TbLeaveDao leaveDao;

    @Autowired
    private ITaskService itaskService;

    @Autowired
    private TbReimDao reimDao;

    @Autowired
    private TaskService taskService;


    /**
     * 查询待办任务
     *
     * @param req
     * @return
     */
    @Override
    public PageUtils findWaitTask(SearchTaskByPageForm req) {


        Integer userId = UserUtils.getUserId();
        HashMap hashMap = userDao.searchById(userId);
        String assignee = (String) hashMap.get("username");//用于请求查询数据库的用户名

        Set<String> permissions = userDao.searchUserPermissions(userId);


        TaskQuery query = taskService.createTaskQuery()
                .taskCandidateOrAssigned(assignee) // 候选人或者办理人
                .orderByTaskCreateTime().asc();

        // 分页查询
        List<Task> taskList = query.listPage(req.getFirstPage(), req.getLength());

        long total = query.count();

        List<Map<String, Object>> records = new ArrayList<>();
        for (Task task : taskList) {
            Map<String, Object> result = new HashMap<>();

            //TODO 待优化，需要改成监听模式
            if (task.getName().equals("请假归档")) {
                //寻找文件归档的权限
                result.put("filing", false);
                for (String permission : permissions) {
                    if (permission.equals("FILE:ARCHIVE")) {
                        result.put("filing", true);
                    }
                }
            }
            if (task.getName().equals("财务归档")) {
                //寻找文件归档的权限
                result.put("filing", false);
                for (String permission : permissions) {
                    if (permission.equals("FILE:ARCHIVE")) {
                        result.put("filing", true);
                    }
                }
            }

            result.put("taskId", task.getId());
            result.put("createDate", DateUtils.format(task.getCreateTime()));//任务名称
            result.put("processInstanceId", task.getProcessInstanceId());
            result.put("executionId", task.getExecutionId());
            result.put("processDefinitionId", task.getProcessDefinitionId());
            // 任务办理人: 如果是候选人则没有值，办理人才有
            result.put("taskAssignee", task.getAssignee());

            // 查询流程实例
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId()).singleResult();
            result.put("processName", pi.getProcessDefinitionName());
            result.put("creatorName", pi.getStartUserId());//任务发起人
            result.put("businessKey", pi.getBusinessKey());

            String processDefinitionKey = pi.getProcessDefinitionKey();
            String processKey = processDefinitionKey.split(":")[0];
            if (processKey.equals("ConferenceApp")) {

                result.put("type", "会议申请");
                HashMap tbMeeting = meetingDao.searchMeetingInfoByBusinessKey(pi.getBusinessKey());
                result.put("status", MeetingStatusEnum.getEumByCode(
                        (Integer) tbMeeting.get("status")).getDesc());//当前任务状态
                result.put("title", tbMeeting.get("name") + "的会议申请");

            } else if (processKey.equals("LeaveProcess")) {
                //请假申请

                result.put("type", "员工请假");
                HashMap map = new HashMap();
                map.put("businessKey", pi.getBusinessKey());
                HashMap leave = leaveDao.searchLeaveByBusinessKey(map);
                result.put("title", leave.get("name") + "的请假申请");
                LeaveStatusEnum leaveStatusEnum =
                        LeaveStatusEnum.getEumByCode((Integer) leave.get("status"));
                result.put("status", leaveStatusEnum.getDesc());

            } else if (processKey.equals("reimLeave")) {
                //报销管理
                result.put("type", "报销申请");
                HashMap map = new HashMap();
                map.put("id", pi.getBusinessKey());
                HashMap reim = reimDao.searchReimById(map);
                ReimStatusEnum reimStatusEnum = ReimStatusEnum.getEumByCode((Integer) reim.get("status"));
                result.put("title", reim.get("name") + "的报销申请");
                result.put("status", reimStatusEnum.getDesc());
            }

            records.add(result);
        }


        PageUtils pageUtils = new PageUtils(records, total,
                req.getPage(), req.getLength());
        return pageUtils;
    }

    /**
     * 查询已办任务
     *
     * @param req
     * @return
     */
    @Override
    public PageUtils findCompleteTask(SearchTaskByPageForm req) {

        int userId = StpUtil.getLoginIdAsInt();
        HashMap userInfo = userDao.searchUserInfo(userId);
        String assignee = (String) userInfo.get("username");
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(assignee) // 办理人
                .orderByTaskCreateTime().desc()
                .finished();// 任务已办理


        List<HistoricTaskInstance> historicTaskInstances =
                query.listPage(req.getFirstPage(), req.getLength());


        long total = query.count();

        List<HashMap<String, Object>> records = new ArrayList<>();
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
            HashMap<String, Object> result = new HashMap<>();

            result.put("filing", false);
            result.put("taskId", historicTaskInstance.getId());
            result.put("taskName", historicTaskInstance.getName());//任务名称
            result.put("createDate", DateUtils.format(historicTaskInstance.getCreateTime()));//任务审批时间
            result.put("processInstanceId", historicTaskInstance.getProcessInstanceId());
            result.put("executionId", historicTaskInstance.getExecutionId());
            result.put("processDefinitionId", historicTaskInstance.getProcessDefinitionId());
            // 任务办理人: 如果是候选人则没有值，办理人才有
            result.put("taskAssignee", historicTaskInstance.getAssignee());

            // 查询流程实例
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(historicTaskInstance.getProcessInstanceId()).singleResult();
            result.put("processName", pi.getProcessDefinitionName());
            result.put("creatorName", pi.getStartUserId());//任务发起人
            result.put("businessKey", pi.getBusinessKey());
            String processDefinitionKey = pi.getProcessDefinitionKey();

            String processKey = processDefinitionKey.split(":")[0];
            String businessKey = pi.getBusinessKey();

            if (processKey.equals("ConferenceApp")) {
                result.put("type", "会议申请");
                HashMap tbMeeting = meetingDao.searchMeetingInfoByBusinessKey(businessKey);
                MeetingStatusEnum meetingStatusEnum = MeetingStatusEnum.getEumByCode((Integer) tbMeeting.get("status"));
                result.put("status", meetingStatusEnum.getDesc());//当前任务状态
                result.put("title", tbMeeting.get("name") + "的会议申请");

            } else if (processKey.equals("LeaveProcess")) {
                //请假申请
                result.put("type", "员工请假");
                HashMap map = new HashMap();
                map.put("businessKey", businessKey);
                HashMap leave = leaveDao.searchLeaveByBusinessKey(map);
                LeaveStatusEnum leaveStatusEnum = LeaveStatusEnum.getEumByCode((Integer) leave.get("status"));
                result.put("status", leaveStatusEnum.getDesc());//当前任务状态
                result.put("title", leave.get("name") + "的请假申请");

            } else if (processKey.equals("reimLeave")) {
                //报销管理
                HashMap map = new HashMap();
                map.put("id", pi.getBusinessKey());
                HashMap reim = reimDao.searchReimById(map);
                ReimStatusEnum reimStatusEnum = ReimStatusEnum.getEumByCode((Integer) reim.get("status"));
                result.put("status", reimStatusEnum.getDesc());
                result.put("amount", reim.get("amount"));
                Integer typeId = (Integer) reim.get("type_id");


                result.put("type", "报销申请");
                result.put("anleihen", reim.get("anleihen"));
                result.put("balance", reim.get("balance"));
            }

            records.add(result);
        }


        PageUtils pageUtils = new PageUtils(records, total, req.getPage(), req.getLength());
        return pageUtils;
    }

    @Override
    public HashMap searchApprovalContent(HashMap param) {
        String type = (String) param.get("type");
        String businessKey = (String) param.get("businessKey");

        HashMap result = new HashMap();
        if (type.equals("会议申请")) {
            HashMap meetingInfo = meetingDao.searchMeetingInfoByBusinessKey(businessKey);
            result.put("desc", meetingInfo.get("desc"));
            result.put("date", meetingInfo.get("date"));
            result.put("place", meetingInfo.get("place"));
            result.put("start", meetingInfo.get("start"));
            result.put("end", meetingInfo.get("end"));
            result.put("name", meetingInfo.get("name"));
        } else if (type.equals("员工请假")) {
            HashMap map = new HashMap();
            map.put("businessKey", businessKey);
            HashMap leaveInfo = leaveDao.searchLeaveByBusinessKey(map);
            result.put("reason", leaveInfo.get("reason"));
            result.put("type", leaveInfo.get("type"));
            result.put("start", leaveInfo.get("start"));
            result.put("end", leaveInfo.get("end"));
            result.put("name", leaveInfo.get("name"));
            String fileUrl = (String) leaveInfo.get("file_url");
            if (fileUrl != null) {
                List<FileUrl> fileUrls = JSON.parseArray(fileUrl, FileUrl.class);
                result.put("files", fileUrls);
            }
        } else if (type.equals("报销申请")) {
            HashMap map = new HashMap();
            map.put("id", businessKey);
            HashMap reimInfo = reimDao.searchReimById(map);
            result.put("reason", reimInfo.get("name") + "的报销申请");
            result.put("type", "报销申请");
            result.put("typeId", reimInfo.get("type_id"));
            result.put("start", reimInfo.get("start"));
            result.put("name", reimInfo.get("name"));
            result.put("amount", reimInfo.get("amount"));
            result.put("balance", reimInfo.get("balance"));
            result.put("anleihen", reimInfo.get("anleihen"));
            String fileUrl = (String) reimInfo.get("file_url");
            if (fileUrl != null) {
                List<FileUrl> fileUrls = JSON.parseArray(fileUrl, FileUrl.class);
                result.put("files", fileUrls);
            }
        }
        return result;
    }

    @Override
    public void getHistoryProcessImage(String prodInstId, HttpServletResponse response) {
        InputStream inputStream = null;
        try {
            // 1.查询流程实例历史数据
            HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(prodInstId).singleResult();

            // 2. 查询流程中已执行的节点，按时开始时间降序排列
            List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(prodInstId)
                    .orderByHistoricActivityInstanceStartTime().desc()
                    .list();

            // 3. 单独的提取高亮节点id ( 绿色）
            List<String> highLightedActivityIdList =
                    historicActivityInstanceList.stream()
                            .map(HistoricActivityInstance::getActivityId).collect(Collectors.toList());

            // 4. 正在执行的节点 （红色）
            List<Execution> runningActivityInstanceList = runtimeService.createExecutionQuery()
                    .processInstanceId(prodInstId).list();

            List<String> runningActivityIdList = new ArrayList<>();
            for (Execution execution : runningActivityInstanceList) {
                if (StringUtils.isNotEmpty(execution.getActivityId())) {
                    runningActivityIdList.add(execution.getActivityId());
                }
            }

            // 获取流程定义Model对象
            BpmnModel bpmnModel = repositoryService.getBpmnModel(instance.getProcessDefinitionId());

            // 实例化流程图生成器
            CustomProcessDiagramGenerator generator = new CustomProcessDiagramGenerator();
            // 获取高亮连线id
            List<String> highLightedFlows = generator.getHighLightedFlows(bpmnModel, historicActivityInstanceList);
            // 生成历史流程图
            inputStream = generator.generateDiagramCustom(bpmnModel, highLightedActivityIdList,
                    runningActivityIdList, highLightedFlows,
                    "宋体", "微软雅黑", "黑体");

            // 响应相关图片
            response.setContentType("image/svg+xml");
            byte[] bytes = IOUtils.toByteArray(inputStream);
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public R completeTask(HashMap param) {
        String taskId = (String) param.get("taskId");
        String approval = (String) param.get("approval");
        //1. 查询任务信息

        Task task = taskService.createTaskQuery().taskId(taskId)
                .singleResult();
        if (task == null)
            return R.error("任务不存在");

        String procInstId = task.getProcessInstanceId();
        // 2. 指定任务审批意见
        taskService.addComment(taskId, procInstId, approval);

        //报销申请审批
        if (StringUtils.equals(task.getProcessDefinitionId().split(":")[0], "reimLeave")) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("comment", approval);
            taskService.complete(taskId, map);
        } else {
            // 3. 完成其他流程
            taskService.complete(taskId);
        }
        // 4. 查询下一个任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(procInstId).list();

        // 5. 指定办理人
        if (CollectionUtils.isEmpty(taskList)) {
            // task.getBusinessKey() m5版本中没有 值
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(procInstId).singleResult();

            if (StringUtils.equals(hpi.getProcessDefinitionKey(),
                    "LeaveProcess")) {
                HashMap map = new HashMap();
                map.put("id", hpi.getBusinessKey());
                map.put("status", LeaveStatusEnum.APPROVAL.getCode());
                return leaveDao.updateLeaveInstanceId(map) > 0 ? R.ok() : R.error();
            } else if (StringUtils.equals(hpi.getProcessDefinitionKey(),
                    "ConferenceApp")) {
                HashMap map = new HashMap();
                map.put("uuid", hpi.getBusinessKey());
                map.put("status", MeetingStatusEnum.APPROVALED.getCode());
                return meetingDao.updateMeetingInstanceId(map) > 0 ? R.ok() : R.error();
            } else if (StringUtils.equals(hpi.getProcessDefinitionKey(),
                    "reimLeave")) {
                HashMap map = new HashMap();
                map.put("id", hpi.getBusinessKey());
                map.put("status", ReimStatusEnum.APPROVAL.getCode());
                return reimDao.updateReimInstanceId(map) > 0 ? R.ok() : R.error();
            }
            // 更新业务状态已完成
         /*   TbBusinessStatus businessStatus = new TbBusinessStatus();
            businessStatus.setBusinessKey(hpi.getBusinessKey());
            businessStatusDao.update(businessStatus);*/
        }

        return R.ok();
    }

    @Override
    public R archiveTask(HashMap param) {

        //调用工作流完成任务
        param.put("approval", "同意");
        this.completeTask(param);

        /*//TODO 报销文件处理
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
                .finished() // 已结束的
                .orderByProcessInstanceEndTime().desc();

        query.processInstanceNameLikeIgnoreCase((String) param.get("processName"));

        HistoricProcessInstance historicProcessInstance = query.singleResult();
        historicProcessInstance.getProcessDefinitionName();
*/
        String processName=((String) param.get("processName"));
        if (processName.equals("报销申请流程")) {
            String businessKey = (String) param.get("businessKey");
            HashMap map = new HashMap();
            map.put("id", businessKey);
            map.put("fileUrl", param.get("files"));
            map.put("status",ReimStatusEnum.APPROVAL.getCode());
            return reimDao.updateReimInstanceId(map) > 0 ? R.ok("归档成功") : R.error("归档失败");
        } else {
            //将Leave实例查询出来
            String businessKey = (String) param.get("businessKey");
            HashMap map = new HashMap();
            map.put("businessKey", businessKey);
            HashMap leave = leaveDao.searchLeaveByBusinessKey(map);
            if (leave != null) {
                leave.put("fileUrl", param.get("files"));
                int rows = leaveDao.updateLeaveBusinessKey(leave);
                return rows > 0 ? R.ok("归档成功") : R.error("归档失败");
            }
        }

        return R.error("任务提交成功");
    }


}
