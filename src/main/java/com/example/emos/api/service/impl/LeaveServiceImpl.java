package com.example.emos.api.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbLeaveDao;
import com.example.emos.api.db.pojo.TbBusinessStatus;
import com.example.emos.api.db.pojo.TbLeave;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.BusinessStatusService;
import com.example.emos.api.service.LeaveService;
import com.example.emos.api.workflow.service.impl.ProcessInstanceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
public class LeaveServiceImpl implements LeaveService {
    @Autowired
    private TbLeaveDao leaveDao;

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private BusinessStatusService businessStatusService;

    @Override
    public PageUtils searchLeaveByPage(HashMap param) {
        ArrayList<HashMap> list = leaveDao.searchLeaveByPage(param);
        long count = leaveDao.searchLeaveCount(param);
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    public boolean searchContradiction(HashMap param) {
        long count = leaveDao.searchContradiction(param);
        boolean bool = count > 0;
        return bool;
    }

    @Override
    public int insert(TbLeave leave) {
        int rows = leaveDao.insert(leave);
        if (rows != 1)
            throw new EmosException("请假添加失败");
       /* else {
            //添加业务状态信息
            TbBusinessStatus businessStatus = new TbBusinessStatus();
            businessStatus.setBusinessKey(leave.getId());
            businessStatus.setCreateDate(new Date());
            businessStatus.setUpdateDate(new Date());
            TbBusinessStatus result = businessStatusService.insert(businessStatus);

            if (result != null)
                return 1;

        }*/
        return rows;
    }

    @Override
    public int deleteLeaveById(HashMap param) {
        String id = MapUtil.getStr(param, "id");
        String instanceId = MapUtil.getStr(param, "instanceId");
        int rows = leaveDao.deleteLeaveById(param);
        if (rows == 1 && !StringUtils.isEmpty(instanceId)) {
            int row = processInstanceService.cancel(id, instanceId, "删除请假申请");
            return row;
        }
        return rows;
    }

    ;

    @Override
    public HashMap searchLeaveById(HashMap param) {
        HashMap map = leaveDao.searchLeaveById(param);
        return map;
    }
}
