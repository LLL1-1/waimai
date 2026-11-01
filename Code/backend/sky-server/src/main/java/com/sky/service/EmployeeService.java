package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.EmployeeLoginVO;
import com.sky.vo.EmployeeVO;

/**
 * 员工业务接口。
 *
 * <p>包含登录、分页、新增、修改、详情和启用/禁用。</p>
 */
public interface EmployeeService {

    /**
     * 员工登录。
     *
     * @param employeeLoginDTO 用户名和密码
     * @return 登录结果，包含 JWT token
     */
    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 分页查询员工。
     *
     * @param employeePageQueryDTO 分页与姓名筛选条件
     * @return 分页结果
     */
    PageResult<EmployeeVO> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 新增员工。
     *
     * @param employeeDTO 员工信息，密码必填
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 修改员工。
     *
     * @param employeeDTO 员工信息，id 必填；密码为空表示不修改
     */
    void update(EmployeeDTO employeeDTO);

    /**
     * 根据 id 查询员工。
     *
     * @param id 员工 id
     * @return 员工信息
     */
    EmployeeVO getById(Long id);

    /**
     * 启用或禁用员工。
     *
     * @param status 目标状态：0 禁用，1 启用
     * @param id     员工 id
     */
    void startOrStop(Integer status, Long id);
}
