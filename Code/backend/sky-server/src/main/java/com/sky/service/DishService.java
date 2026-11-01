package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

/**
 * 菜品业务接口。
 */
public interface DishService {

    /**
     * 分页查询菜品。
     *
     * @param dishPageQueryDTO 分页与筛选条件
     * @return 分页结果
     */
    PageResult<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 新增菜品，同时保存口味。
     *
     * @param dishDTO 菜品信息
     */
    void save(DishDTO dishDTO);

    /**
     * 修改菜品，同时替换口味。
     *
     * @param dishDTO 菜品信息，id 必填
     */
    void update(DishDTO dishDTO);

    /**
     * 删除菜品，同时删除口味。
     *
     * @param ids 菜品 id 列表
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据 id 查询菜品，包含分类名称和口味。
     *
     * @param id 菜品 id
     * @return 菜品详情
     */
    DishVO getById(Long id);

    /**
     * 根据分类查询菜品列表。
     *
     * @param categoryId 分类 id，可为空
     * @return 菜品列表
     */
    List<DishVO> list(Long categoryId);

    /**
     * 起售或停售菜品。
     *
     * @param status 目标状态：0 停售，1 起售
     * @param id     菜品 id
     */
    void startOrStop(Integer status, Long id);
}
