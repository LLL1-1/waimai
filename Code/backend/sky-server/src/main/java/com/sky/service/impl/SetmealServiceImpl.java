package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BusinessException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
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
 * 套餐业务实现。
 *
 * <p>套餐和菜品是一对多关系：新增/修改套餐时在同一个事务里维护 setmeal_dish，
 * 删除套餐前检查是否被订单明细引用。</p>
 */
@Service
@RequiredArgsConstructor
public class SetmealServiceImpl implements SetmealService {

    /** 未登录时的兜底用户 id；正常请求会从 BaseContext 取当前登录人 */
    private static final Long DEFAULT_USER_ID = 1L;

    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final CategoryMapper categoryMapper;

    /**
     * 分页查询套餐，返回分类名称。
     *
     * @param setmealPageQueryDTO 分页与筛选条件
     * @return 分页结果
     */
    @Override
    public PageResult<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        int pageNum = setmealPageQueryDTO.getPage() == null || setmealPageQueryDTO.getPage() < 1
                ? 1 : setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize() == null || setmealPageQueryDTO.getPageSize() < 1
                ? 10 : setmealPageQueryDTO.getPageSize();

        Page<Setmeal> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(setmealPageQueryDTO.getName()),
                        Setmeal::getName, setmealPageQueryDTO.getName())
                .eq(setmealPageQueryDTO.getCategoryId() != null,
                        Setmeal::getCategoryId, setmealPageQueryDTO.getCategoryId())
                .eq(setmealPageQueryDTO.getStatus() != null,
                        Setmeal::getStatus, setmealPageQueryDTO.getStatus())
                .orderByDesc(Setmeal::getUpdateTime);

        setmealMapper.selectPage(page, wrapper);

        Map<Long, String> categoryNames = getCategoryNames(page.getRecords());
        List<SetmealVO> records = page.getRecords().stream()
                .map(setmeal -> toVO(setmeal, categoryNames.get(setmeal.getCategoryId())))
                .toList();
        return new PageResult<>(page.getTotal(), records);
    }

    /**
     * 新增套餐。
     *
     * <p>校验分类必须是套餐分类、名称不重复，然后保存套餐和菜品关系。</p>
     *
     * @param setmealDTO 套餐信息
     */
    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        validateCategory(setmealDTO.getCategoryId());
        checkNameUnique(setmealDTO.getName(), null);

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 新增时 id 由数据库自增
        setmeal.setId(null);
        setmeal.setStatus(setmealDTO.getStatus() == null
                ? StatusConstant.ENABLE : setmealDTO.getStatus());

        LocalDateTime now = LocalDateTime.now();
        setmeal.setCreateTime(now);
        setmeal.setUpdateTime(now);
        setmeal.setCreateUser(currentUserId());
        setmeal.setUpdateUser(currentUserId());
        setmealMapper.insert(setmeal);

        saveSetmealDishes(setmeal.getId(), setmealDTO.getSetmealDishes());
    }

    /**
     * 修改套餐。
     *
     * <p>setmealDishes 传了才替换；不传则保留原套餐菜品关系。</p>
     *
     * @param setmealDTO 套餐信息，id 必填
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        if (setmealDTO.getId() == null) {
            throw new BusinessException("套餐id不能为空");
        }
        Setmeal existing = setmealMapper.selectById(setmealDTO.getId());
        if (existing == null) {
            throw new BusinessException("套餐不存在");
        }

        validateCategory(setmealDTO.getCategoryId());
        checkNameUnique(setmealDTO.getName(), setmealDTO.getId());

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(currentUserId());
        setmealMapper.updateById(setmeal);

        // setmealDishes 为 null 表示不改关系；传空数组表示清空
        if (setmealDTO.getSetmealDishes() != null) {
            setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>()
                    .eq(SetmealDish::getSetmealId, setmealDTO.getId()));
            saveSetmealDishes(setmealDTO.getId(), setmealDTO.getSetmealDishes());
        }
    }

    /**
     * 批量删除套餐。
     *
     * <p>被订单明细引用的套餐不允许删除，避免历史订单缺套餐。</p>
     *
     * @param ids 套餐 id 列表
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的套餐");
        }

        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.selectById(id);
            if (setmeal == null) {
                throw new BusinessException("套餐不存在");
            }
            if (setmealMapper.countOrderDetail(id) > 0) {
                throw new BusinessException("套餐被订单引用，不能删除");
            }
            setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>()
                    .eq(SetmealDish::getSetmealId, id));
            setmealMapper.deleteById(id);
        }
    }

    /**
     * 根据 id 查询套餐详情，包含分类名称和套餐菜品。
     *
     * @param id 套餐 id
     * @return 套餐详情
     */
    @Override
    public SetmealVO getById(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        if (setmeal == null) {
            throw new BusinessException("套餐不存在");
        }

        Category category = categoryMapper.selectById(setmeal.getCategoryId());
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(
                new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, id));

        SetmealVO setmealVO = toVO(setmeal, category == null ? null : category.getName());
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 根据分类查询套餐列表。
     *
     * @param categoryId 分类 id，可为空
     * @return 套餐列表
     */
    @Override
    public List<SetmealVO> list(Long categoryId) {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, Setmeal::getCategoryId, categoryId)
                .orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmeals = setmealMapper.selectList(wrapper);
        Map<Long, String> categoryNames = getCategoryNames(setmeals);
        return setmeals.stream()
                .map(setmeal -> toVO(setmeal, categoryNames.get(setmeal.getCategoryId())))
                .toList();
    }

    /**
     * 起售或停售套餐。
     *
     * @param status 目标状态：0 停售，1 起售
     * @param id     套餐 id
     */
    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        if (id == null) {
            throw new BusinessException("套餐id不能为空");
        }
        if (status == null || (status != StatusConstant.ENABLE && status != StatusConstant.DISABLE)) {
            throw new BusinessException("套餐状态只能是0或1");
        }

        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(currentUserId());

        int rows = setmealMapper.updateById(setmeal);
        if (rows == 0) {
            throw new BusinessException("套餐不存在");
        }
    }

    /**
     * 校验分类存在且属于套餐分类（type=2）。
     *
     * @param categoryId 分类 id
     */
    private void validateCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException("套餐分类不能为空");
        }
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (category.getType() == null || category.getType() != 2) {
            throw new BusinessException("套餐只能关联套餐分类");
        }
    }

    /**
     * 校验套餐名称唯一；修改时排除自己。
     *
     * @param name 套餐名称
     * @param id   当前套餐 id，新增时为 null
     */
    private void checkNameUnique(String name, Long id) {
        Long count = setmealMapper.selectCount(new LambdaQueryWrapper<Setmeal>()
                .eq(Setmeal::getName, name)
                .ne(id != null, Setmeal::getId, id));
        if (count > 0) {
            throw new BusinessException("套餐名称已存在");
        }
    }

    /**
     * 批量保存套餐菜品关系。
     *
     * @param setmealId     套餐 id
     * @param setmealDishes 套餐菜品列表
     */
    private void saveSetmealDishes(Long setmealId, List<SetmealDish> setmealDishes) {
        if (setmealDishes == null || setmealDishes.isEmpty()) {
            return;
        }
        for (SetmealDish setmealDish : setmealDishes) {
            // 关系 id 由数据库生成，setmealId 以当前套餐为准
            setmealDish.setId(null);
            setmealDish.setSetmealId(setmealId);
            setmealDishMapper.insert(setmealDish);
        }
    }

    /**
     * 查询分类 id 到分类名称的映射。
     *
     * @param setmeals 套餐列表
     * @return 分类名称映射
     */
    private Map<Long, String> getCategoryNames(List<Setmeal> setmeals) {
        Set<Long> categoryIds = setmeals.stream()
                .map(Setmeal::getCategoryId)
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
     * @param setmeal      套餐实体
     * @param categoryName 分类名称
     * @return 套餐 VO
     */
    private SetmealVO toVO(Setmeal setmeal, String categoryName) {
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setCategoryName(categoryName);
        return setmealVO;
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
