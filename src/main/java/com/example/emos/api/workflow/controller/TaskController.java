package com.example.emos.api.workflow.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.emos.api.cmd.DelelteExecutionCommand;
import com.example.emos.api.cmd.DeleteTaskCommand;
import com.example.emos.api.common.util.R;
import com.example.emos.api.common.util.UserUtils;
import com.example.emos.api.controller.form.ApprovalTaskForm;
import com.example.emos.api.service.BusinessStatusService;
import com.example.emos.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "查询任务", description = "任务管理控制层")
@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    TaskService taskService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    TaskRuntime taskRuntime;

    @Autowired
    HistoryService historyService;

    @Autowired
    BusinessStatusService businessStatusService;

    @Autowired
    UserService userService;

    @Operation(summary = "获取历史任务节点，用于驳回功能")
    @GetMapping("/back/nodes")
    public List<Map<String, Object>> getBackNodes(String taskId) {
        try {
            HashMap map = userService.searchById(StpUtil.getLoginIdAsInt());
            String username = (String) map.get("username");
            Task task = taskService.createTaskQuery()
                    .taskId(taskId)
                    .taskAssignee(username)
                    .singleResult();
            if (task == null) {
                return null;
            }
            // 查询历史任务节点
           /* List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .list();*/
            // 不把当前节点查询出来, 没有办理完的节点不查询，每条数据都有一个唯一值，我们使用随机数
            String sql = "select rand() AS ID_, t2.* from " +
                    " ( select distinct t1.TASK_DEF_KEY_, t1.NAME_ from " +
                    "  ( select ID_, RES.TASK_DEF_KEY_, RES.NAME_, RES.START_TIME_, RES.END_TIME_ " +
                    "   from ACT_HI_TASKINST RES " +
                    "   WHERE RES.PROC_INST_ID_ = #{processInstanceId} and TASK_DEF_KEY_ != #{taskDefKey}" +
                    "   and RES.END_TIME_ is not null order by RES.START_TIME_ asc " +
                    "  ) t1 " +
                    " ) t2";

            List<HistoricTaskInstance> list = historyService.createNativeHistoricTaskInstanceQuery()
                    .sql(sql)
                    .parameter("processInstanceId", task.getProcessInstanceId())
                    .parameter("taskDefKey", task.getTaskDefinitionKey()) // 不把当前节点查询出来
                    .list();

            List<Map<String, Object>> records = new ArrayList<>();

            for (HistoricTaskInstance hti : list) {
                Map<String, Object> data = new HashMap<>();
                data.put("activityId", hti.getTaskDefinitionKey());
                data.put("activityName", hti.getName());
                records.add(data);
            }

            return records;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Autowired
    private ManagementService managementService;

    @Operation(summary = "驳回历史节点")
    @PostMapping("/back")
    public R backProcess(@RequestBody ApprovalTaskForm form
    ) {

        String taskId = form.getTaskId();

        try {
            // 1. 查询当前任务信息
            Task task = taskService.createTaskQuery()
                    .taskId(taskId)
                    .taskAssignee((String) userService.searchById(StpUtil.getLoginIdAsInt()).get("username"))
                    .singleResult();
            if (task == null) {
                return R.error("当前任务不存在或你不是任务办理人");
            }

            if (!task.getProcessDefinitionId().split(":")[0].equals("reimLeave")) {

                String procInstId = task.getProcessInstanceId();

                List<Map<String, Object>> backNodes = getBackNodes(taskId);
                Map<String, Object> map = backNodes.get(0);
                String activityId = (String) map.get("activityId");

                // 2. 获取流程模型实例 BpmnModel
                BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
                // 3. 当前节点信息
                FlowNode curFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
                // 4. 获取当前节点的原出口连线
                List<SequenceFlow> sequenceFlowList = curFlowNode.getOutgoingFlows();
                // 5. 临时存储当前节点的原出口连线
                List<SequenceFlow> oriSequenceFlows = new ArrayList<>();
                oriSequenceFlows.addAll(sequenceFlowList);
                // 6. 将当前节点的原出口清空
                sequenceFlowList.clear();

                // 7. 获取目标节点信息
                FlowNode targetFlowNode = (FlowNode) bpmnModel.getFlowElement(activityId);
                // 8. 获取驳回的新节点
                // 获取目标节点的入口连线
                List<SequenceFlow> incomingFlows = targetFlowNode.getIncomingFlows();
                // 存储所有目标出口
                List<SequenceFlow> allSequenceFlow = new ArrayList<>();
                for (SequenceFlow incomingFlow : incomingFlows) {
                    // 找到入口连线的源头（获取目标节点的父节点）
                    FlowNode source = (FlowNode) incomingFlow.getSourceFlowElement();
                    List<SequenceFlow> sequenceFlows;
                    if (source instanceof ParallelGateway) {
                        // 并行网关: 获取目标节点的父节点（并行网关）的所有出口，
                        sequenceFlows = source.getOutgoingFlows();
                    } else {
                        // 其他类型父节点, 则获取目标节点的入口连续
                        sequenceFlows = targetFlowNode.getIncomingFlows();
                    }
                    allSequenceFlow.addAll(sequenceFlows);
                }

                // 9. 将当前节点的出口设置为新节点
                curFlowNode.setOutgoingFlows(allSequenceFlow);

                // 10. 完成当前任务，流程就会流向目标节点创建新目标任务
                //      删除已完成任务，删除已完成并行任务的执行数据 act_ru_execution
                List<Task> list = taskService.createTaskQuery().processInstanceId(procInstId).list();
                for (Task t : list) {
                    if (taskId.equals(t.getId())) {
                        // 当前任务，完成当前任务
                        String message = String.format("【%s 驳回任务 %s => %s】",
                                UserUtils.getUsername(), task.getName(), targetFlowNode.getName());
                        taskService.addComment(t.getId(), procInstId, message);
                        // 完成任务，就会进行驳回到目标节点，产生目标节点的任务数据
                        taskService.complete(taskId);
                        // 删除执行表中 is_active_ = 0的执行数据， 使用command自定义模型
                        DelelteExecutionCommand deleteExecutionCMD = new DelelteExecutionCommand(task.getExecutionId());
                        managementService.executeCommand(deleteExecutionCMD);
                    } else {
                        // 删除其他未完成的并行任务
                        // taskService.deleteTask(taskId); // 注意这种方式删除不掉，会报错：流程正在运行中无法删除。
                        // 使用command自定义命令模型来删除，直接操作底层的删除表对应的方法，对应的自定义是否删除
                        DeleteTaskCommand deleteTaskCMD = new DeleteTaskCommand(t.getId());
                        managementService.executeCommand(deleteTaskCMD);
                    }
                }

                // 13. 完成驳回功能后，将当前节点的原出口方向进行恢复
                curFlowNode.setOutgoingFlows(oriSequenceFlows);


                // 12. 查询目标任务节点历史办理人
                List<Task> newTaskList = taskService.createTaskQuery().processInstanceId(procInstId).list();
                for (Task newTask : newTaskList) {
                    // 取之前的历史办理人
                    HistoricTaskInstance oldTargerTask = historyService.createHistoricTaskInstanceQuery()
                            .taskDefinitionKey(newTask.getTaskDefinitionKey()) // 节点id
                            .processInstanceId(procInstId)
                            .finished() // 已经完成才是历史
                            .orderByTaskCreateTime().desc() // 最新办理的在最前面
                            .list().get(0);
                    taskService.setAssignee(newTask.getId(), oldTargerTask.getAssignee());
                }

                return R.ok();
            } else {
                //reimLeave流程的不同意逻辑
                HashMap<String, Object> map = new HashMap<>();
                map.put("comment", form.getApproval());
                taskService.setVariable(taskId,"comment",form.getApproval());
                taskService.complete(taskId, map);
                return R.ok();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("驳回失败：" + e.getMessage());
        }

    }


}




























