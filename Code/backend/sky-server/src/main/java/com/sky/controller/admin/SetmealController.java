package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
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
 * 管理端套餐接口。
 *
 * <p>所有接口都在 /admin/** 下，需要 JWT token。</p>
 */
@RestController
@RequestMapping("/admin/setmeal")
@RequiredArgsConstructor
@Tag(name = "套餐管理")
public class SetmealController {

    private final SetmealService setmealService;

    /**
     * 分页查询套餐。
     *
     * @param setmealPageQueryDTO 分页与筛选条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "套餐分页查询")
    public Result<PageResult<SetmealVO>> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        return Result.success(setmealService.pageQuery(setmealPageQueryDTO));
    }

    /**
     * 新增套餐。
     *
     * @param setmealDTO 套餐信息，包含套餐菜品列表
     * @return 成功响应
     */
    @PostMapping
    @Operation(summary = "新增套餐")
    public Result<Void> save(@RequestBody @Valid SetmealDTO setmealDTO) {
        setmealService.save(setmealDTO);
        return Result.success();
    }

    /**
     * 修改套餐。
     *
     * @param setmealDTO 套餐信息，id 必填
     * @return 成功响应
     */
    @PutMapping
    @Operation(summary = "修改套餐")
    public Result<Void> update(@RequestBody @Valid SetmealDTO setmealDTO) {
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 批量删除套餐。
     *
     * @param ids 套餐 id 列表，例如 1,2
     * @return 成功响应
     */
    @DeleteMapping
    @Operation(summary = "批量删除套餐")
    public Result<Void> delete(@RequestParam List<Long> ids) {
        setmealService.deleteByIds(ids);
        return Result.success();
    }

    /**
     * 根据 id 查询套餐详情。
     *
     * @param id 套餐 id
     * @return 套餐详情，包含分类名称和套餐菜品
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        return Result.success(setmealService.getById(id));
    }

    /**
     * 根据分类查询套餐列表。
     *
     * @param categoryId 分类 id，可为空
     * @return 套餐列表
     */
    @GetMapping("/list")
    @Operation(summary = "根据分类查询套餐")
    public Result<List<SetmealVO>> list(@RequestParam(required = false) Long categoryId) {
        return Result.success(setmealService.list(categoryId));
    }

    /**
     * 起售或停售套餐。
     *
     * @param status 目标状态：0 停售，1 起售
     * @param id     套餐 id
     * @return 成功响应
     */
    @PostMapping("/status/{status}")
    @Operation(summary = "起售或停售套餐")
    public Result<Void> startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        setmealService.startOrStop(status, id);
        return Result.success();
    }
}
