package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端菜品接口。
 *
 * <p>所有接口都在 /admin/** 下，需要 JWT token。</p>
 */
@RestController
@RequestMapping("/admin/dish")
@RequiredArgsConstructor
@Tag(name = "菜品管理")
public class DishController {

    private final DishService dishService;

    /**
     * 分页查询菜品。
     *
     * @param dishPageQueryDTO 分页与筛选条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "菜品分页查询")
    public Result<PageResult<DishVO>> page(DishPageQueryDTO dishPageQueryDTO) {
        return Result.success(dishService.pageQuery(dishPageQueryDTO));
    }

    /**
     * 新增菜品。
     *
     * @param dishDTO 菜品信息，包含口味列表
     * @return 成功响应
     */
    @PostMapping
    @Operation(summary = "新增菜品")
    public Result<Void> save(@RequestBody @Valid DishDTO dishDTO) {
        dishService.save(dishDTO);
        return Result.success();
    }

    /**
     * 修改菜品。
     *
     * @param dishDTO 菜品信息，id 必填
     * @return 成功响应
     */
    @PutMapping
    @Operation(summary = "修改菜品")
    public Result<Void> update(@RequestBody @Valid DishDTO dishDTO) {
        dishService.update(dishDTO);
        return Result.success();
    }

    /**
     * 批量删除菜品。
     *
     * @param ids 菜品 id 列表，例如 1,2
     * @return 成功响应
     */
    @DeleteMapping
    @Operation(summary = "批量删除菜品")
    public Result<Void> delete(@RequestParam List<Long> ids) {
        dishService.deleteByIds(ids);
        return Result.success();
    }

    /**
     * 根据 id 查询菜品详情。
     *
     * @param id 菜品 id
     * @return 菜品详情，包含分类名称和口味
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        return Result.success(dishService.getById(id));
    }

    /**
     * 根据分类查询菜品列表。
     *
     * @param categoryId 分类 id，可为空
     * @return 菜品列表
     */
    @GetMapping("/list")
    @Operation(summary = "根据分类查询菜品")
    public Result<List<DishVO>> list(@RequestParam(required = false) Long categoryId) {
        return Result.success(dishService.list(categoryId));
    }

    /**
     * 起售或停售菜品。
     *
     * @param status 目标状态：0 停售，1 起售
     * @param id     菜品 id
     * @return 成功响应
     */
    @PostMapping("/status/{status}")
    @Operation(summary = "起售或停售菜品")
    public Result<Void> startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        dishService.startOrStop(status, id);
        return Result.success();
    }
}
