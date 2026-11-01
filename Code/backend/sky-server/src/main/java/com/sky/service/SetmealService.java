package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

/**
 * 套餐业务接口。
 */
public interface SetmealService {

    /**
     * 分页查询套餐。
     *
     * @param setmealPageQueryDTO 分页与筛选条件
     * @return 分页结果
     */
    PageResult<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 新增套餐，同时保存套餐菜品关系。
     *
     * @param setmealDTO 套餐信息
     */
    void save(SetmealDTO setmealDTO);

    /**
     * 修改套餐，同时替换套餐菜品关系。
     *
     * @param setmealDTO 套餐信息，id 必填
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 批量删除套餐，同时删除套餐菜品关系。
     *
     * @param ids 套餐 id 列表
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据 id 查询套餐，包含分类名称和套餐菜品。
     *
     * @param id 套餐 id
     * @return 套餐详情
     */
    SetmealVO getById(Long id);

    /**
     * 根据分类查询套餐列表。
     *
     * @param categoryId 分类 id，可为空
     * @return 套餐列表
     */
    List<SetmealVO> list(Long categoryId);

    /**
     * 起售或停售套餐。
     *
     * @param status 目标状态：0 停售，1 起售
     * @param id     套餐 id
     */
    void startOrStop(Integer status, Long id);
}
