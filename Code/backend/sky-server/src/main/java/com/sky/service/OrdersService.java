package com.sky.service;

import com.sky.dto.OrderPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrdersSubmitVO;
import com.sky.vo.OrdersVO;

/** 订单业务接口。 */
public interface OrdersService {

    OrdersSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    void pay(Long id);

    PageResult<Orders> pageQuery(Integer status, Integer page, Integer pageSize);

    OrdersVO detail(Long id);

    void cancel(Long id);

    PageResult<Orders> adminPage(OrderPageQueryDTO orderPageQueryDTO);

    OrdersVO adminDetail(Long id);

    void confirm(Long id);

    void reject(Long id, String rejectionReason);

    void delivery(Long id);

    void complete(Long id);
}
