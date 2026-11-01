package com.sky.vo;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/** 订单详情返回，继承订单字段并附带明细。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrdersVO extends Orders {
    private static final long serialVersionUID = 1L;

    private List<OrderDetail> details;
}
