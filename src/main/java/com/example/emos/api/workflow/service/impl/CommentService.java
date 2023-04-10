package com.example.emos.api.workflow.service.impl;

import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component(value = "commentService")
public class CommentService {

    public void isComment(DelegateExecution delegateExecution) {
        String comment = (String)delegateExecution.getVariable("comment");
        if ("同意".equals(comment)) {
            delegateExecution.setVariable("comment", comment);
        } else {
            delegateExecution.setVariable("comment", comment);
        }
    }
}
