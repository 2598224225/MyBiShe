package com.example.emos.api.db.dao;

import com.example.emos.api.db.pojo.TbBusinessStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 业务状态实体表(TbBusinessStatus)表数据库访问层
 *
 * @author makejava
 * @since 2022-04-12 23:32:38
 */
@Mapper
public interface TbBusinessStatusDao {

    /**
     * 通过ID查询单条数据
     *
     * @param businessKey 主键
     * @return 实例对象
     */
    TbBusinessStatus queryById(String businessKey);

    /**
     * 查询指定行数据
     *
     * @param tbBusinessStatus 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
    List<TbBusinessStatus> queryAllByLimit(TbBusinessStatus tbBusinessStatus, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param tbBusinessStatus 查询条件
     * @return 总行数
     */
    long count(TbBusinessStatus tbBusinessStatus);

    /**
     * 新增数据
     *
     * @param tbBusinessStatus 实例对象
     * @return 影响行数
     */
    int insert(TbBusinessStatus tbBusinessStatus);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<TbBusinessStatus> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<TbBusinessStatus> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<TbBusinessStatus> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<TbBusinessStatus> entities);

    /**
     * 修改数据
     *
     * @param tbBusinessStatus 实例对象
     * @return 影响行数
     */
    int update(TbBusinessStatus tbBusinessStatus);

    /**
     * 通过主键删除数据
     *
     * @param businessKey 主键
     * @return 影响行数
     */
    int deleteById(String businessKey);

}

