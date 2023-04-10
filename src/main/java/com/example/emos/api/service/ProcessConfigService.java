package com.example.emos.api.service;

import com.example.emos.api.db.pojo.TbProcessConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * 流程定义配置表(TbProcessConfig)表服务接口
 *
 * @author makejava
 * @since 2022-04-12 23:30:23
 */
public interface ProcessConfigService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    TbProcessConfig queryById(String id);

    /**
     * 分页查询
     *
     * @param tbProcessConfig 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
    Page<TbProcessConfig> queryByPage(TbProcessConfig tbProcessConfig, PageRequest pageRequest);

    /**
     * 新增数据
     *
     * @param tbProcessConfig 实例对象
     * @return 实例对象
     */
    TbProcessConfig insert(TbProcessConfig tbProcessConfig);

    /**
     * 修改数据
     *
     * @param tbProcessConfig 实例对象
     * @return 实例对象
     */
    TbProcessConfig update(TbProcessConfig tbProcessConfig);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(String id);

    TbProcessConfig getByBusinessRoute(String businessRoute);
}
