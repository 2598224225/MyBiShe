package com.example.emos.api.listener;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

public class MyExecutionListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) {
        if(StringUtils.equals(execution.getEventName(), "take")){
            System.out.println(execution.getEventName());
            System.out.println(execution.getId());
            System.out.println(execution.getCurrentActivityId());
        }else if(StringUtils.equals(execution.getEventName(), "end")){
            //【2】流程定义 以及 当前活动节点
            String currentExecutionId = execution.getId();//正在执行的流程对象的执行id
            String currentActivityId = execution.getCurrentActivityId();//正在执行的流程对象的活动节点id 获取当前的.Activityid
            FlowElement flowElement = execution.getCurrentFlowElement();//正在执行的流程对象的活动节点名称
            String processInstanceId = execution.getProcessInstanceId();//当前流程实例id
            String processDefinitionId = execution.getProcessDefinitionId();//流程定义id
            String parentExecutionId = execution.getParentId();//获取父id，并发的时候有用

            String tenantId = execution.getTenantId();//获取TenantId 当有多个TenantId 有用

            String businessKey = execution.getProcessInstanceBusinessKey();//业务id已经废弃


            /**
             * 这个非常有用吧。当拿到EngineServices 对象所有的xxxService都可以拿到
             */

        }
    }
{
    }
}
