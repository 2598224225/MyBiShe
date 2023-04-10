package com.example.emos.api.db.req;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Tag(name="",description = "启动流程实例请求类（提交申请)")
public class StartREQ implements Serializable {

    private String businessRoute;

    private String businessKey;

    private List<String> assignees;

    private Map<String, Object> variables;

    public Map<String, Object> getVariables() {
        return variables == null ? new HashMap<>() : variables;
    }

}
