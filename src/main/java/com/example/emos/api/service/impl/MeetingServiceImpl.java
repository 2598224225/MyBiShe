package com.example.emos.api.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbMeetingDao;
import com.example.emos.api.db.pojo.TbBusinessStatus;
import com.example.emos.api.db.pojo.TbMeeting;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.BusinessStatusService;
import com.example.emos.api.service.MeetingService;
import com.example.emos.api.workflow.service.impl.ProcessInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class MeetingServiceImpl implements MeetingService {
    @Autowired
    private TbMeetingDao meetingDao;


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BusinessStatusService businessStatusService;

    @Autowired
    private ProcessInstanceService processInstanceService;


    @Override
    public PageUtils searchOfflineMeetingByPage(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchOfflineMeetingByPage(param);
        long count = meetingDao.searchOfflineMeetingCount(param);
        int start = MapUtil.getInt(param, "start");
        int length = MapUtil.getInt(param, "length");
        for (HashMap map : list) {
            String meeting = (String) map.get("meeting");
            if (meeting != null && meeting.length() > 0) {
                map.replace("meeting", JSONUtil.parseArray(meeting));
            }
        }
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

   /* @Override
    public int insert(TbMeeting meeting) {
        int rows = meetingDao.insert(meeting);
        if (rows != 1) {
            throw new EmosException("会议添加失败");
        }
        meetingWorkflowTask.startMeetingWorkflow(meeting.getUuid(), meeting.getCreatorId(), meeting.getTitle(),
                meeting.getDate(), meeting.getStart() + ":00", meeting.getType() == 1 ? "线上会议" : "线下会议");
        return rows;
    }*/

    @Override
    public ArrayList<HashMap> searchOfflineMeetingInWeek(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchOfflineMeetingInWeek(param);
        return list;
    }

    @Override
    public HashMap searchMeetingInfo(short status, long id) {
        //判断正在进行中的会议
        HashMap map;
        if (status == 4) {
            map = meetingDao.searchCurrentMeetingInfo(id);
        } else {
            map = meetingDao.searchMeetingInfo(id);
        }
        return map;
    }

    @Override
    @Transactional
    public int deleteMeetingApplication(HashMap param) {
        Long id = MapUtil.getLong(param, "id");
        String uuid = MapUtil.getStr(param, "uuid");
        String instanceId = MapUtil.getStr(param, "instanceId");
        //查询会议详情，一会儿要判断是否距离会议开始不足20分钟
        HashMap meeting = meetingDao.searchMeetingById(param);
        String date = MapUtil.getStr(meeting, "date");
        String start = MapUtil.getStr(meeting, "start");
        int status = MapUtil.getInt(meeting, "status");
        boolean isCreator = Boolean.parseBoolean(MapUtil.getStr(meeting, "isCreator"));
        DateTime dateTime = DateUtil.parse(date + " " + start);
        DateTime now = DateUtil.date();
        if (now.isAfterOrEquals(dateTime.offset(DateField.MINUTE, -20))) {
            throw new EmosException("距离会议开始不足20分钟，不能删除会议");
        }
        //只能申请人删除该会议
        if (!isCreator) {
            throw new EmosException("只能申请人删除该会议");
        }
        //待审批、未开始、尚为提交的会议可以删除
        if (status == 1 || status == 3 || status == 0) {
            int rows = meetingDao.deleteMeetingApplication(param);
            //已经提交的会议才可以取消会议
            if (rows == 1 && !StringUtils.isEmpty(instanceId)) {
                String reason = MapUtil.getStr(param, "reason");
                int row = processInstanceService.cancel(uuid, instanceId, reason);
                return row;
            }
            return rows;
        } else {
            throw new EmosException("只能删除待审批和未开始的会议");
        }
    }

    @Override
    public PageUtils searchOnlineMeetingByPage(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchOnlineMeetingByPage(param);
        //生成线上会议房间号
        for (HashMap map : list) {
            String uuid = (String) map.get("uuid");
            Long time = (Long) map.get("time");
            Random random = new Random(100000);
            int i = random.nextInt(100000);
            String s = String.valueOf(i);
            long roomId = Long.parseLong(s);
            redisTemplate.opsForValue().set(uuid, roomId);
        }
        long count = meetingDao.searchOnlineMeetingCount(param);
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    public Long searchRoomIdByUUID(String uuid) {
        if (redisTemplate.hasKey(uuid)) {
            Object temp = redisTemplate.opsForValue().get(uuid);
            long roomId = Long.parseLong(temp.toString());
            return roomId;
        }
        return null;
    }

    @Override
    public ArrayList<HashMap> searchOnlineMeetingMembers(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchOnlineMeetingMembers(param);
        return list;
    }

    @Override
    public boolean searchCanCheckinMeeting(HashMap param) {
        long count = meetingDao.searchCanCheckinMeeting(param);
        return count == 1 ? true : false;
    }

    @Override
    public int updateMeetingPresent(HashMap param) {
        int rows = meetingDao.updateMeetingPresent(param);
        return rows;
    }

    @Override
    @Transactional
    public int insert(TbMeeting meeting) {

        int row = meetingDao.insert(meeting);

        if (row != 1)
            throw new EmosException("会议添加失败");
       /* else {
            //添加业务状态信息
            TbBusinessStatus businessStatus = new TbBusinessStatus();
            businessStatus.setBusinessKey(meeting.getUuid());
            businessStatus.setCreateDate(new Date());
            businessStatus.setUpdateDate(new Date());
            TbBusinessStatus result = businessStatusService.insert(businessStatus);

            if (result != null) {
                return 1;
            }

        }*/

        return row;


    }


}
