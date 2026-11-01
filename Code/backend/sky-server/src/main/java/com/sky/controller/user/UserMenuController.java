package com.sky.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.Category;
import com.sky.exception.BusinessException;
import com.sky.mapper.CategoryMapper;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 用户端菜单：分类、起售菜品、起售套餐。 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户端菜单")
public class UserMenuController {

    private final CategoryMapper categoryMapper;
    private final DishService dishService;
    private final SetmealService setmealService;

    @GetMapping("/category/list")
    @Operation(summary = "分类列表")
    public Result<List<Category>> categories(@RequestParam(defaultValue = "1") Integer type) {
        return Result.success(categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getType, type)
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)));
    }

    @GetMapping("/dish/list")
    @Operation(summary = "起售菜品列表")
    public Result<List<DishVO>> dishes(@RequestParam(required = false) Long categoryId) {
        List<DishVO> list = dishService.list(categoryId).stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == 1)
                .toList();
        return Result.success(list);
    }

    @GetMapping("/dish/{id}")
    @Operation(summary = "菜品详情")
    public Result<DishVO> dish(@PathVariable Long id) {
        DishVO dish = dishService.getById(id);
        if (dish.getStatus() == null || dish.getStatus() != 1) {
            throw new BusinessException("菜品已停售");
        }
        return Result.success(dish);
    }

    @GetMapping("/setmeal/list")
    @Operation(summary = "起售套餐列表")
    public Result<List<SetmealVO>> setmeals(@RequestParam(required = false) Long categoryId) {
        List<SetmealVO> list = setmealService.list(categoryId).stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == 1)
                .toList();
        return Result.success(list);
    }

    @GetMapping("/setmeal/{id}")
    @Operation(summary = "套餐详情")
    public Result<SetmealVO> setmeal(@PathVariable Long id) {
        SetmealVO setmeal = setmealService.getById(id);
        if (setmeal.getStatus() == null || setmeal.getStatus() != 1) {
            throw new BusinessException("套餐已停售");
        }
        return Result.success(setmeal);
    }
}
