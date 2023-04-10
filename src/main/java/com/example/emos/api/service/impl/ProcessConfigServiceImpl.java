package com.example.emos.api.service.impl;

import com.example.emos.api.db.pojo.TbProcessConfig;
import com.example.emos.api.db.dao.TbProcessConfigDao;
import com.example.emos.api.service.ProcessConfigService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;

/**
 * 流程定义配置表(TbProcessConfig)表服务实现类
 *
 * @author makejava
 * @since 2022-04-12 23:30:23
 */
@Service("tbProcessConfigService")
public class ProcessConfigServiceImpl implements ProcessConfigService {
    @Resource
    private TbProcessConfigDao tbProcessConfigDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public TbProcessConfig queryById(String id) {
        return this.tbProcessConfigDao.queryById(id);
    }

    /**
     * 分页查询
     *
     * @param tbProcessConfig 筛选条件
     * @param pageRequest     分页对象
     * @return 查询结果
     */
    @Override
    public Page<TbProcessConfig> queryByPage(TbProcessConfig tbProcessConfig, PageRequest pageRequest) {
        long total = this.tbProcessConfigDao.count(tbProcessConfig);
        return new PageImpl<>(this.tbProcessConfigDao.queryAllByLimit(tbProcessConfig, pageRequest), pageRequest, total);
    }

    /**
     * 新增数据
     *
     * @param tbProcessConfig 实例对象
     * @return 实例对象
     */
    @Override
    public TbProcessConfig insert(TbProcessConfig tbProcessConfig) {
        this.tbProcessConfigDao.insert(tbProcessConfig);
        return tbProcessConfig;
    }

    /**
     * 修改数据
     *
     * @param tbProcessConfig 实例对象
     * @return 实例对象
     */
    @Override
    public TbProcessConfig update(TbProcessConfig tbProcessConfig) {
        this.tbProcessConfigDao.update(tbProcessConfig);
        return this.queryById(tbProcessConfig.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(String id) {
        return this.tbProcessConfigDao.deleteById(id) > 0;
    }

    @Override
    public TbProcessConfig getByBusinessRoute(String businessRoute) {
        if ("OfflineMeeting".equals(businessRoute)) {

            return tbProcessConfigDao.queryByBusinessRoute(businessRoute);
        } else if ("OnlineMeeting".equals(businessRoute)) {
            return tbProcessConfigDao.queryByBusinessRoute("OfflineMeeting");
        }
        return tbProcessConfigDao.queryByBusinessRoute(businessRoute);
    }
}
