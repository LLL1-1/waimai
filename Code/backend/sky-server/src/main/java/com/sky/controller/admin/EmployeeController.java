package com.sky.controller.admin;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.vo.EmployeeLoginVO;
import com.sky.vo.EmployeeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端员工接口。
 *
 * <p>登录接口放行；其余员工管理接口都需要 JWT token。</p>
 */
@RestController
@RequestMapping("/admin/employee")
@RequiredArgsConstructor
@Tag(name = "员工管理")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 员工登录。
     *
     * @param employeeLoginDTO 用户名和密码
     * @return 登录结果，包含 token
     */
    @PostMapping("/login")
    @Operation(summary = "员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody @Valid EmployeeLoginDTO employeeLoginDTO) {
        return Result.success(employeeService.login(employeeLoginDTO));
    }

    /**
     * 分页查询员工。
     *
     * @param employeePageQueryDTO 分页与姓名筛选条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "员工分页查询")
    public Result<PageResult<EmployeeVO>> page(EmployeePageQueryDTO employeePageQueryDTO) {
        return Result.success(employeeService.pageQuery(employeePageQueryDTO));
    }

    /**
     * 新增员工。
     *
     * @param employeeDTO 员工信息
     * @return 成功响应
     */
    @PostMapping
    @Operation(summary = "新增员工")
    public Result<Void> save(@RequestBody @Valid EmployeeDTO employeeDTO) {
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 修改员工。
     *
     * @param employeeDTO 员工信息，id 必填
     * @return 成功响应
     */
    @PutMapping
    @Operation(summary = "修改员工")
    public Result<Void> update(@RequestBody @Valid EmployeeDTO employeeDTO) {
        employeeService.update(employeeDTO);
        return Result.success();
    }

    /**
     * 根据 id 查询员工。
     *
     * @param id 员工 id
     * @return 员工信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id查询员工")
    public Result<EmployeeVO> getById(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }

    /**
     * 启用或禁用员工。
     *
     * @param status 目标状态：0 禁用，1 启用
     * @param id     员工 id
     * @return 成功响应
     */
    @PostMapping("/status/{status}")
    @Operation(summary = "启用或禁用员工")
    public Result<Void> startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        employeeService.startOrStop(status, id);
        return Result.success();
    }
}
