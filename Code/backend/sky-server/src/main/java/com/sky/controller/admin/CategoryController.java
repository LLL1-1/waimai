package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
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
 * 管理端分类接口。
 *
 * <p>统一前缀 /admin/category；当前登录/JWT 还没接入，接口暂时开放。</p>
 */
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
@Tag(name = "分类管理")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 新增分类。
     *
     * @param categoryDTO 分类信息
     * @return 成功响应
     */
    @PostMapping
    @Operation(summary = "新增分类")
    public Result<Void> save(@RequestBody @Valid CategoryDTO categoryDTO) {
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 修改分类。
     *
     * @param categoryDTO 分类信息，id 必填
     * @return 成功响应
     */
    @PutMapping
    @Operation(summary = "修改分类")
    public Result<Void> update(@RequestBody @Valid CategoryDTO categoryDTO) {
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 删除分类。
     *
     * @param id 分类 id
     * @return 成功响应
     */
    @DeleteMapping
    @Operation(summary = "删除分类")
    public Result<Void> delete(@RequestParam Long id) {
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 根据 id 查询分类。
     *
     * @param id 分类 id
     * @return 分类信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id查询分类")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    /**
     * 查询分类列表。
     *
     * @param type 分类类型，可不传
     * @return 分类列表
     */
    @GetMapping("/list")
    @Operation(summary = "分类列表")
    public Result<List<Category>> list(@RequestParam(required = false) Integer type) {
        return Result.success(categoryService.list(type));
    }

    /**
     * 分页查询分类。
     *
     * @param categoryPageQueryDTO 分页与筛选条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分类分页查询")
    public Result<PageResult<Category>> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        return Result.success(categoryService.pageQuery(categoryPageQueryDTO));
    }

    /**
     * 启用或禁用分类。
     *
     * @param status 目标状态：0 禁用，1 启用
     * @param id     分类 id
     * @return 成功响应
     */
    @PostMapping("/status/{status}")
    @Operation(summary = "启用或禁用分类")
    public Result<Void> startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        categoryService.startOrStop(status, id);
        return Result.success();
    }
}
