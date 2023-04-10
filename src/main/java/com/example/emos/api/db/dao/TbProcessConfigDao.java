package com.example.emos.api.db.dao;

import com.example.emos.api.db.pojo.TbProcessConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 流程定义配置表(TbProcessConfig)表数据库访问层
 *
 * @author hsr
 * @since 2022-04-12 23:30:18
 */
@Mapper
public interface TbProcessConfigDao {

    /**
     * 查询流程配置数据
     * @param processKey 流程定义key
     * @return
     */
    TbProcessConfig getByProcessKey(String processKey);

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    TbProcessConfig queryById(String id);

    /**
     * 查询指定行数据
     *
     * @param tbProcessConfig 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
    List<TbProcessConfig> queryAllByLimit(TbProcessConfig tbProcessConfig, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param tbProcessConfig 查询条件
     * @return 总行数
     */
    long count(TbProcessConfig tbProcessConfig);

    /**
     * 新增数据
     *
     * @param tbProcessConfig 实例对象
     * @return 影响行数
     */
    int insert(TbProcessConfig tbProcessConfig);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<TbProcessConfig> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<TbProcessConfig> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<TbProcessConfig> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<TbProcessConfig> entities);

    /**
     * 修改数据
     *
     * @param tbProcessConfig 实例对象
     * @return 影响行数
     */
    int update(TbProcessConfig tbProcessConfig);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(String id);

    TbProcessConfig queryByBusinessRoute(String businessRoute);
}

