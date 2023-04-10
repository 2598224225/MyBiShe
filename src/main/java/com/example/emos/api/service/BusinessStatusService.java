package com.example.emos.api.service;

import com.example.emos.api.db.pojo.TbBusinessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * 业务状态实体表(TbBusinessStatus)表服务接口
 *
 * @author makejava
 * @since 2022-04-12 23:32:38
 */
public interface BusinessStatusService {

    /**
     * 通过ID查询单条数据
     *
     * @param businessKey 主键
     * @return 实例对象
     */
    TbBusinessStatus queryById(String businessKey);

    /**
     * 分页查询
     *
     * @param tbBusinessStatus 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
    Page<TbBusinessStatus> queryByPage(TbBusinessStatus tbBusinessStatus, PageRequest pageRequest);

    /**
     * 新增数据
     *
     * @param tbBusinessStatus 实例对象
     * @return 实例对象
     */
    TbBusinessStatus insert(TbBusinessStatus tbBusinessStatus);

    /**
     * 修改数据
     *
     * @param tbBusinessStatus 实例对象
     * @return 实例对象
     */
    TbBusinessStatus update(TbBusinessStatus tbBusinessStatus);

    /**
     * 通过主键删除数据
     *
     * @param businessKey 主键
     * @return 是否成功
     */
    boolean deleteById(String businessKey);

}
