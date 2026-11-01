package com.sky.service;

/**
 * 店铺营业状态业务接口。
 */
public interface ShopService {

    /**
     * 获取店铺营业状态。
     *
     * @return 1 营业中，0 打烊
     */
    Integer getStatus();

    /**
     * 设置店铺营业状态。
     *
     * @param status 1 营业中，0 打烊
     */
    void setStatus(Integer status);
}
