package com.example.emos.api.service.impl;

import com.example.emos.api.db.pojo.TbBusinessStatus;
import com.example.emos.api.db.dao.TbBusinessStatusDao;
import com.example.emos.api.service.BusinessStatusService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;

/**
 * 业务状态实体表(TbBusinessStatus)表服务实现类
 *
 * @author makejava
 * @since 2022-04-12 23:32:38
 */
@Service("tbBusinessStatusService")
public class BusinessStatusServiceImpl implements BusinessStatusService {
    @Resource
    private TbBusinessStatusDao tbBusinessStatusDao;

    /**
     * 通过ID查询单条数据
     *
     * @param businessKey 主键
     * @return 实例对象
     */
    @Override
    public TbBusinessStatus queryById(String businessKey) {
        return this.tbBusinessStatusDao.queryById(businessKey);
    }

    /**
     * 分页查询
     *
     * @param tbBusinessStatus 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
    @Override
    public Page<TbBusinessStatus> queryByPage(TbBusinessStatus tbBusinessStatus, PageRequest pageRequest) {
        long total = this.tbBusinessStatusDao.count(tbBusinessStatus);
        return new PageImpl<>(this.tbBusinessStatusDao.queryAllByLimit(tbBusinessStatus, pageRequest), pageRequest, total);
    }

    /**
     * 新增数据
     *
     * @param tbBusinessStatus 实例对象
     * @return 实例对象
     */
    @Override
    public TbBusinessStatus insert(TbBusinessStatus tbBusinessStatus) {
        this.tbBusinessStatusDao.insert(tbBusinessStatus);
        return tbBusinessStatus;
    }

    /**
     * 修改数据
     *
     * @param tbBusinessStatus 实例对象
     * @return 实例对象
     */
    @Override
    public TbBusinessStatus update(TbBusinessStatus tbBusinessStatus) {
        this.tbBusinessStatusDao.update(tbBusinessStatus);
        return this.queryById(tbBusinessStatus.getBusinessKey());
    }

    /**
     * 通过主键删除数据
     *
     * @param businessKey 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(String businessKey) {
        return this.tbBusinessStatusDao.deleteById(businessKey) > 0;
    }
}
