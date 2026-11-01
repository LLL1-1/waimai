package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.exception.BusinessException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类业务实现。
 *
 * <p>create_user/update_user 优先取 BaseContext 中的当前登录人；
 * 未经过拦截器时（例如单元测试）回退为 1。</p>
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    /** 未登录时的兜底用户 id；正常请求会从 BaseContext 取当前登录人 */
    private static final Long DEFAULT_USER_ID = 1L;

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    /**
     * 新增分类。
     *
     * <p>先校验类型和名称唯一，再补齐状态、排序和审计字段。</p>
     *
     * @param categoryDTO 分类信息
     */
    @Override
    @Transactional
    public void save(CategoryDTO categoryDTO) {
        // 类型只能是 1（菜品分类）或 2（套餐分类）
        validateType(categoryDTO.getType());

        // 分类名称在数据库有唯一索引，这里先查一次，给出更友好的提示
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, categoryDTO.getName()));
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        // 新增时 id 由数据库自增，避免 DTO 里误传 id
        category.setId(null);
        // 不传状态默认启用，不传排序默认 0
        category.setStatus(categoryDTO.getStatus() == null ? StatusConstant.ENABLE : categoryDTO.getStatus());
        category.setSort(categoryDTO.getSort() == null ? 0 : categoryDTO.getSort());
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(currentUserId());
        category.setUpdateUser(currentUserId());
        categoryMapper.insert(category);
    }

    /**
     * 修改分类。
     *
     * <p>名称唯一性校验要排除当前记录；status/sort 不传时保留原值。</p>
     *
     * @param categoryDTO 分类信息，id 必填
     */
    @Override
    @Transactional
    public void update(CategoryDTO categoryDTO) {
        if (categoryDTO.getId() == null) {
            throw new BusinessException("分类id不能为空");
        }

        Category existing = categoryMapper.selectById(categoryDTO.getId());
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        validateType(categoryDTO.getType());

        // 排除自己之后，名称不能和其他分类重复
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, categoryDTO.getName())
                .ne(Category::getId, categoryDTO.getId()));
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        // 未传状态/排序时保留数据库原值，避免被 null 覆盖
        category.setStatus(categoryDTO.getStatus() == null ? existing.getStatus() : categoryDTO.getStatus());
        category.setSort(categoryDTO.getSort() == null ? existing.getSort() : categoryDTO.getSort());
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(currentUserId());
        categoryMapper.updateById(category);
    }

    /**
     * 根据 id 删除分类。
     *
     * <p>分类被菜品或套餐引用时不允许删除，避免出现孤儿数据。</p>
     *
     * @param id 分类 id
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }

        // 分类被菜品或套餐引用时不允许删除
        Long dishCount = dishMapper.selectCount(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, id));
        if (dishCount > 0) {
            throw new BusinessException("分类下存在菜品，不能删除");
        }
        Long setmealCount = setmealMapper.selectCount(new LambdaQueryWrapper<Setmeal>()
                .eq(Setmeal::getCategoryId, id));
        if (setmealCount > 0) {
            throw new BusinessException("分类下存在套餐，不能删除");
        }

        categoryMapper.deleteById(id);
    }

    /**
     * 根据 id 查询分类，不存在时抛业务异常。
     *
     * @param id 分类 id
     * @return 分类信息
     */
    @Override
    public Category getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return category;
    }

    /**
     * 查询分类列表，可按类型筛选，按 sort 升序。
     *
     * @param type 分类类型，为 null 时查全部
     * @return 分类列表
     */
    @Override
    public List<Category> list(Integer type) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(type != null, Category::getType, type)
                .orderByAsc(Category::getSort);
        return categoryMapper.selectList(wrapper);
    }

    /**
     * 分页查询分类。
     *
     * <p>page/pageSize 做兜底，name 模糊匹配，type 精确匹配。</p>
     *
     * @param categoryPageQueryDTO 分页与筛选条件
     * @return 分页结果
     */
    @Override
    public PageResult<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        // 防止前端传 0 或负数导致分页异常
        int pageNum = categoryPageQueryDTO.getPage() == null || categoryPageQueryDTO.getPage() < 1
                ? 1 : categoryPageQueryDTO.getPage();
        int pageSize = categoryPageQueryDTO.getPageSize() == null || categoryPageQueryDTO.getPageSize() < 1
                ? 10 : categoryPageQueryDTO.getPageSize();

        Page<Category> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(categoryPageQueryDTO.getName()),
                        Category::getName, categoryPageQueryDTO.getName())
                .eq(categoryPageQueryDTO.getType() != null,
                        Category::getType, categoryPageQueryDTO.getType())
                .orderByAsc(Category::getSort);

        categoryMapper.selectPage(page, wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    /**
     * 启用或禁用分类。
     *
     * @param status 目标状态：0 禁用，1 启用
     * @param id     分类 id
     */
    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        if (id == null) {
            throw new BusinessException("分类id不能为空");
        }
        if (status == null || (status != StatusConstant.ENABLE && status != StatusConstant.DISABLE)) {
            throw new BusinessException("分类状态只能是0或1");
        }

        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(currentUserId());
        // updateById 返回受影响行数，0 表示 id 不存在
        int rows = categoryMapper.updateById(category);
        if (rows == 0) {
            throw new BusinessException("分类不存在");
        }
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

    /**
     * 校验分类类型：只允许 1 或 2。
     *
     * @param type 分类类型
     */
    private void validateType(Integer type) {
        if (type == null || (type != 1 && type != 2)) {
            throw new BusinessException("分类类型只能是1或2");
        }
    }
}
