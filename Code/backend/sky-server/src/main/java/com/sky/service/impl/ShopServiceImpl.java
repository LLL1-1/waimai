package com.sky.service.impl;

import com.sky.constant.RedisConstant;
import com.sky.constant.StatusConstant;
import com.sky.exception.BusinessException;
import com.sky.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 店铺营业状态实现。
 *
 * <p>状态存在 Redis 的 SHOP_STATUS 中；没有值时默认营业中。</p>
 */
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取店铺营业状态。
     *
     * @return 1 营业中，0 打烊
     */
    @Override
    public Integer getStatus() {
        String value = stringRedisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS);
        return value == null ? StatusConstant.ENABLE : Integer.valueOf(value);
    }

    /**
     * 设置店铺营业状态。
     *
     * @param status 1 营业中，0 打烊
     */
    @Override
    public void setStatus(Integer status) {
        if (status == null || (status != StatusConstant.ENABLE && status != StatusConstant.DISABLE)) {
            throw new BusinessException("营业状态只能是0或1");
        }
        stringRedisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS, String.valueOf(status));
    }
}
