package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.BusinessException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜品业务实现。
 *
 * <p>菜品和口味是一对多关系：新增/修改菜品时在同一个事务里维护 dish_flavor，
 * 删除菜品前检查是否被套餐引用。</p>
 */
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    /** 未登录时的兜底用户 id；正常请求会从 BaseContext 取当前登录人 */
    private static final Long DEFAULT_USER_ID = 1L;

    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final CategoryMapper categoryMapper;

    /**
     * 分页查询菜品，返回分类名称。
     *
     * @param dishPageQueryDTO 分页与筛选条件
     * @return 分页结果
     */
    @Override
    public PageResult<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        int pageNum = dishPageQueryDTO.getPage() == null || dishPageQueryDTO.getPage() < 1
                ? 1 : dishPageQueryDTO.getPage();
        int pageSize = dishPageQueryDTO.getPageSize() == null || dishPageQueryDTO.getPageSize() < 1
                ? 10 : dishPageQueryDTO.getPageSize();

        Page<Dish> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dishPageQueryDTO.getName()),
                        Dish::getName, dishPageQueryDTO.getName())
                .eq(dishPageQueryDTO.getCategoryId() != null,
                        Dish::getCategoryId, dishPageQueryDTO.getCategoryId())
                .eq(dishPageQueryDTO.getStatus() != null,
                        Dish::getStatus, dishPageQueryDTO.getStatus())
                .orderByDesc(Dish::getUpdateTime);

        dishMapper.selectPage(page, wrapper);

        Map<Long, String> categoryNames = getCategoryNames(page.getRecords());
        List<DishVO> records = page.getRecords().stream()
                .map(dish -> toVO(dish, categoryNames.get(dish.getCategoryId())))
                .toList();
        return new PageResult<>(page.getTotal(), records);
    }

    /**
     * 新增菜品。
     *
     * <p>校验分类必须是菜品分类、名称不重复，然后保存菜品和口味。</p>
     *
     * @param dishDTO 菜品信息
     */
    @Override
    @Transactional
    public void save(DishDTO dishDTO) {
        validateCategory(dishDTO.getCategoryId());
        checkNameUnique(dishDTO.getName(), null);

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 新增时 id 由数据库自增
        dish.setId(null);
        dish.setStatus(dishDTO.getStatus() == null ? StatusConstant.ENABLE : dishDTO.getStatus());

        LocalDateTime now = LocalDateTime.now();
        dish.setCreateTime(now);
        dish.setUpdateTime(now);
        dish.setCreateUser(currentUserId());
        dish.setUpdateUser(currentUserId());
        dishMapper.insert(dish);

        saveFlavors(dish.getId(), dishDTO.getFlavors());
    }

    /**
     * 修改菜品。
     *
     * <p>口味传了才替换；不传则保留原口味。</p>
     *
     * @param dishDTO 菜品信息，id 必填
     */
    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        if (dishDTO.getId() == null) {
            throw new BusinessException("菜品id不能为空");
        }
        Dish existing = dishMapper.selectById(dishDTO.getId());
        if (existing == null) {
            throw new BusinessException("菜品不存在");
        }

        validateCategory(dishDTO.getCategoryId());
        checkNameUnique(dishDTO.getName(), dishDTO.getId());

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(currentUserId());
        dishMapper.updateById(dish);

        // flavors 为 null 表示不改口味；传空数组表示清空口味
        if (dishDTO.getFlavors() != null) {
            dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>()
                    .eq(DishFlavor::getDishId, dishDTO.getId()));
            saveFlavors(dishDTO.getId(), dishDTO.getFlavors());
        }
    }

    /**
     * 批量删除菜品。
     *
     * <p>被套餐引用的菜品不允许删除，避免套餐详情缺菜。</p>
     *
     * @param ids 菜品 id 列表
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的菜品");
        }

        for (Long id : ids) {
            Dish dish = dishMapper.selectById(id);
            if (dish == null) {
                throw new BusinessException("菜品不存在");
            }
            if (dishMapper.countSetmealDish(id) > 0) {
                throw new BusinessException("菜品被套餐引用，不能删除");
            }
            dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>()
                    .eq(DishFlavor::getDishId, id));
            dishMapper.deleteById(id);
        }
    }

    /**
     * 根据 id 查询菜品详情，包含分类名称和口味。
     *
     * @param id 菜品 id
     * @return 菜品详情
     */
    @Override
    public DishVO getById(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }

        Category category = categoryMapper.selectById(dish.getCategoryId());
        List<DishFlavor> flavors = dishFlavorMapper.selectList(
                new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, id));

        DishVO dishVO = toVO(dish, category == null ? null : category.getName());
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 根据分类查询菜品列表。
     *
     * @param categoryId 分类 id，可为空
     * @return 菜品列表
     */
    @Override
    public List<DishVO> list(Long categoryId) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, Dish::getCategoryId, categoryId)
                .orderByDesc(Dish::getUpdateTime);

        List<Dish> dishes = dishMapper.selectList(wrapper);
        Map<Long, String> categoryNames = getCategoryNames(dishes);
        return dishes.stream()
                .map(dish -> toVO(dish, categoryNames.get(dish.getCategoryId())))
                .toList();
    }

    /**
     * 起售或停售菜品。
     *
     * @param status 目标状态：0 停售，1 起售
     * @param id     菜品 id
     */
    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        if (id == null) {
            throw new BusinessException("菜品id不能为空");
        }
        if (status == null || (status != StatusConstant.ENABLE && status != StatusConstant.DISABLE)) {
            throw new BusinessException("菜品状态只能是0或1");
        }

        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(currentUserId());

        int rows = dishMapper.updateById(dish);
        if (rows == 0) {
            throw new BusinessException("菜品不存在");
        }
    }

    /**
     * 校验分类存在且属于菜品分类（type=1）。
     *
     * @param categoryId 分类 id
     */
    private void validateCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException("菜品分类不能为空");
        }
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (category.getType() == null || category.getType() != 1) {
            throw new BusinessException("菜品只能关联菜品分类");
        }
    }

    /**
     * 校验菜品名称唯一；修改时排除自己。
     *
     * @param name 菜品名称
     * @param id   当前菜品 id，新增时为 null
     */
    private void checkNameUnique(String name, Long id) {
        Long count = dishMapper.selectCount(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getName, name)
                .ne(id != null, Dish::getId, id));
        if (count > 0) {
            throw new BusinessException("菜品名称已存在");
        }
    }

    /**
     * 批量保存菜品口味。
     *
     * @param dishId  菜品 id
     * @param flavors 口味列表
     */
    private void saveFlavors(Long dishId, List<DishFlavor> flavors) {
        if (flavors == null || flavors.isEmpty()) {
            return;
        }
        for (DishFlavor flavor : flavors) {
            // 口味 id 由数据库生成，dishId 以当前菜品为准
            flavor.setId(null);
            flavor.setDishId(dishId);
            dishFlavorMapper.insert(flavor);
        }
    }

    /**
     * 查询分类 id 到分类名称的映射。
     *
     * @param dishes 菜品列表
     * @return 分类名称映射
     */
    private Map<Long, String> getCategoryNames(List<Dish> dishes) {
        Set<Long> categoryIds = dishes.stream()
                .map(Dish::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectByIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    /**
     * 实体转 VO。
     *
     * @param dish         菜品实体
     * @param categoryName 分类名称
     * @return 菜品 VO
     */
    private DishVO toVO(Dish dish, String categoryName) {
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setCategoryName(categoryName);
        return dishVO;
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
