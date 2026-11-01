package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.BusinessException;
import com.sky.mapper.EmployeeMapper;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import com.sky.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工业务实现。
 *
 * <p>登录负责签发 JWT；管理端 CRUD 负责维护员工账号。
 * 新增/修改的 create_user、update_user 优先取 BaseContext 中的当前登录人。</p>
 */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    /** 未登录时的兜底用户 id；正常请求会从 BaseContext 取当前登录人 */
    private static final Long DEFAULT_USER_ID = 1L;

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    /**
     * 员工登录：校验账号、密码、状态，成功后签发管理端 JWT。
     *
     * @param employeeLoginDTO 用户名和密码
     * @return 登录结果
     */
    @Override
    public EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO) {
        // 用户名在数据库有唯一索引，最多查出一条
        Employee employee = employeeMapper.selectOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getUsername, employeeLoginDTO.getUsername()));

        if (employee == null) {
            throw new BusinessException("账号不存在");
        }
        if (!passwordEncoder.matches(employeeLoginDTO.getPassword(), employee.getPassword())) {
            throw new BusinessException("密码错误");
        }
        if (employee.getStatus() != null && employee.getStatus() == StatusConstant.DISABLE) {
            throw new BusinessException("账号已被锁定");
        }

        // 把员工 id 写入 token，拦截器解析后放进 BaseContext
        Map<String, Object> claims = new HashMap<>();
        claims.put("empId", employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        return new EmployeeLoginVO(employee.getId(), employee.getUsername(), employee.getName(), token);
    }

    /**
     * 分页查询员工，按更新时间倒序。
     *
     * @param employeePageQueryDTO 分页与姓名筛选条件
     * @return 分页结果
     */
    @Override
    public PageResult<EmployeeVO> pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        int pageNum = employeePageQueryDTO.getPage() == null || employeePageQueryDTO.getPage() < 1
                ? 1 : employeePageQueryDTO.getPage();
        int pageSize = employeePageQueryDTO.getPageSize() == null || employeePageQueryDTO.getPageSize() < 1
                ? 10 : employeePageQueryDTO.getPageSize();

        Page<Employee> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(employeePageQueryDTO.getName()),
                        Employee::getName, employeePageQueryDTO.getName())
                .orderByDesc(Employee::getUpdateTime);

        employeeMapper.selectPage(page, wrapper);

        // 转 VO，避免把 password 返回给前端
        List<EmployeeVO> records = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return new PageResult<>(page.getTotal(), records);
    }

    /**
     * 新增员工。
     *
     * <p>用户名不能重复；密码必须提供并做 BCrypt 加密。</p>
     *
     * @param employeeDTO 员工信息
     */
    @Override
    @Transactional
    public void save(EmployeeDTO employeeDTO) {
        Long count = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getUsername, employeeDTO.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (!StringUtils.hasText(employeeDTO.getPassword())) {
            throw new BusinessException("密码不能为空");
        }

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        // 新增时 id 由数据库自增
        employee.setId(null);
        employee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        employee.setStatus(employeeDTO.getStatus() == null
                ? StatusConstant.ENABLE : employeeDTO.getStatus());

        LocalDateTime now = LocalDateTime.now();
        employee.setCreateTime(now);
        employee.setUpdateTime(now);
        employee.setCreateUser(currentUserId());
        employee.setUpdateUser(currentUserId());
        employeeMapper.insert(employee);
    }

    /**
     * 修改员工。
     *
     * <p>用户名唯一性校验排除自己；password 为空表示保留原密码。</p>
     *
     * @param employeeDTO 员工信息，id 必填
     */
    @Override
    @Transactional
    public void update(EmployeeDTO employeeDTO) {
        if (employeeDTO.getId() == null) {
            throw new BusinessException("员工id不能为空");
        }
        Employee existing = employeeMapper.selectById(employeeDTO.getId());
        if (existing == null) {
            throw new BusinessException("员工不存在");
        }

        Long count = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getUsername, employeeDTO.getUsername())
                .ne(Employee::getId, employeeDTO.getId()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        // 不传密码时保留原密码；传了才重新加密
        if (StringUtils.hasText(employeeDTO.getPassword())) {
            employee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        } else {
            employee.setPassword(null);
        }
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(currentUserId());
        employeeMapper.updateById(employee);
    }

    /**
     * 根据 id 查询员工，不存在时抛业务异常。
     *
     * @param id 员工 id
     * @return 员工信息
     */
    @Override
    public EmployeeVO getById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        return toVO(employee);
    }

    /**
     * 启用或禁用员工。
     *
     * <p>不允许禁用当前登录账号，避免管理员把自己锁死。</p>
     *
     * @param status 目标状态：0 禁用，1 启用
     * @param id     员工 id
     */
    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        if (id == null) {
            throw new BusinessException("员工id不能为空");
        }
        if (status == null || (status != StatusConstant.ENABLE && status != StatusConstant.DISABLE)) {
            throw new BusinessException("员工状态只能是0或1");
        }

        Long currentId = BaseContext.getCurrentId();
        if (status == StatusConstant.DISABLE && currentId != null && currentId.equals(id)) {
            throw new BusinessException("不能禁用当前登录账号");
        }

        Employee employee = new Employee();
        employee.setId(id);
        employee.setStatus(status);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(currentUserId());

        int rows = employeeMapper.updateById(employee);
        if (rows == 0) {
            throw new BusinessException("员工不存在");
        }
    }

    /**
     * 实体转 VO，去掉 password 等敏感字段。
     *
     * @param employee 员工实体
     * @return 员工 VO
     */
    private EmployeeVO toVO(Employee employee) {
        EmployeeVO employeeVO = new EmployeeVO();
        BeanUtils.copyProperties(employee, employeeVO);
        return employeeVO;
    }

    /**
     * 获取当前登录人 id；没有登录上下文时回退为 1。
     *
     * @return 用户 id
     */
    private Long currentUserId() {
        Long currentId = BaseContext.getCurrentId();
        return currentId != null ? currentId : DEFAULT_USER_ID;
    }
}
