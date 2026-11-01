package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.BusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/** 购物车实现。 */
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartMapper shoppingCartMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    @Override
    public List<ShoppingCart> list() {
        Long userId = currentUserId();
        return shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .orderByDesc(ShoppingCart::getCreateTime));
    }

    @Override
    public void add(ShoppingCartDTO dto) {
        Long userId = currentUserId();
        String name;
        String image;
        java.math.BigDecimal price;

        if (dto.getDishId() != null) {
            Dish dish = dishMapper.selectById(dto.getDishId());
            if (dish == null || dish.getStatus() == null || dish.getStatus() != 1) {
                throw new BusinessException("菜品不存在或已停售");
            }
            name = dish.getName();
            image = dish.getImage();
            price = dish.getPrice();
        } else if (dto.getSetmealId() != null) {
            Setmeal setmeal = setmealMapper.selectById(dto.getSetmealId());
            if (setmeal == null || setmeal.getStatus() == null || setmeal.getStatus() != 1) {
                throw new BusinessException("套餐不存在或已停售");
            }
            name = setmeal.getName();
            image = setmeal.getImage();
            price = setmeal.getPrice();
        } else {
            throw new BusinessException("菜品或套餐不能同时为空");
        }

        ShoppingCart existing = find(userId, dto);
        if (existing != null) {
            existing.setNumber(existing.getNumber() + 1);
            shoppingCartMapper.updateById(existing);
            return;
        }

        ShoppingCart cart = new ShoppingCart();
        cart.setName(name);
        cart.setImage(image);
        cart.setUserId(userId);
        cart.setDishId(dto.getDishId());
        cart.setSetmealId(dto.getSetmealId());
        cart.setDishFlavor(dto.getDishFlavor());
        cart.setNumber(1);
        cart.setAmount(price);
        cart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(cart);
    }

    @Override
    public void sub(ShoppingCartDTO dto) {
        ShoppingCart existing = find(currentUserId(), dto);
        if (existing == null) {
            return;
        }
        if (existing.getNumber() > 1) {
            existing.setNumber(existing.getNumber() - 1);
            shoppingCartMapper.updateById(existing);
        } else {
            shoppingCartMapper.deleteById(existing.getId());
        }
    }

    @Override
    public void clean() {
        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, currentUserId()));
    }

    private ShoppingCart find(Long userId, ShoppingCartDTO dto) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .eq(dto.getDishId() != null, ShoppingCart::getDishId, dto.getDishId())
                .eq(dto.getSetmealId() != null, ShoppingCart::getSetmealId, dto.getSetmealId());
        if (StringUtils.hasText(dto.getDishFlavor())) {
            wrapper.eq(ShoppingCart::getDishFlavor, dto.getDishFlavor());
        } else {
            wrapper.isNull(ShoppingCart::getDishFlavor);
        }
        return shoppingCartMapper.selectOne(wrapper);
    }

    private Long currentUserId() {
        Long id = BaseContext.getCurrentId();
        if (id == null) {
            throw new BusinessException("未登录");
        }
        return id;
    }
}
