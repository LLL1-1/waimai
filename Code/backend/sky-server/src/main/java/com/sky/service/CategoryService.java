package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

/**
 * 分类业务接口。
 *
 * <p>管理端分类的增删改查、分页和启用/禁用都走这里。</p>
 */
public interface CategoryService {

    /**
     * 新增分类。
     *
     * @param categoryDTO 分类信息
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 修改分类。
     *
     * @param categoryDTO 分类信息，id 必填
     */
    void update(CategoryDTO categoryDTO);

    /**
     * 根据 id 删除分类。
     *
     * @param id 分类 id
     */
    void deleteById(Long id);

    /**
     * 根据 id 查询分类。
     *
     * @param id 分类 id
     * @return 分类信息
     */
    Category getById(Long id);

    /**
     * 查询分类列表。
     *
     * @param type 分类类型，为 null 时查全部
     * @return 分类列表
     */
    List<Category> list(Integer type);

    /**
     * 分页查询分类。
     *
     * @param categoryPageQueryDTO 分页与筛选条件
     * @return 分页结果
     */
    PageResult<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 启用或禁用分类。
     *
     * @param status 目标状态：0 禁用，1 启用
     * @param id     分类 id
     */
    void startOrStop(Integer status, Long id);
}
