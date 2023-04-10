package com.example.emos.api.controller;

import com.example.emos.api.db.pojo.TbProcessConfig;
import com.example.emos.api.service.ProcessConfigService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 流程定义配置表(TbProcessConfig)表控制层
 *
 * @author makejava
 * @since 2022-04-12 23:30:18
 */
@RestController
@RequestMapping("tbProcessConfig")
public class TbProcessConfigController {
    /**
     * 服务对象
     */
    @Resource
    private ProcessConfigService tbProcessConfigService;

    /**
     * 分页查询
     *
     * @param tbProcessConfig 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
    @GetMapping
    public ResponseEntity<Page<TbProcessConfig>> queryByPage(TbProcessConfig tbProcessConfig, PageRequest pageRequest) {
        return ResponseEntity.ok(this.tbProcessConfigService.queryByPage(tbProcessConfig, pageRequest));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public ResponseEntity<TbProcessConfig> queryById(@PathVariable("id") String id) {
        return ResponseEntity.ok(this.tbProcessConfigService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param tbProcessConfig 实体
     * @return 新增结果
     */
    @PostMapping
    public ResponseEntity<TbProcessConfig> add(TbProcessConfig tbProcessConfig) {
        return ResponseEntity.ok(this.tbProcessConfigService.insert(tbProcessConfig));
    }

    /**
     * 编辑数据
     *
     * @param tbProcessConfig 实体
     * @return 编辑结果
     */
    @PutMapping
    public ResponseEntity<TbProcessConfig> edit(TbProcessConfig tbProcessConfig) {
        return ResponseEntity.ok(this.tbProcessConfigService.update(tbProcessConfig));
    }

    /**
     * 删除数据
     *
     * @param id 主键
     * @return 删除是否成功
     */
    @DeleteMapping
    public ResponseEntity<Boolean> deleteById(String id) {
        return ResponseEntity.ok(this.tbProcessConfigService.deleteById(id));
    }

}

