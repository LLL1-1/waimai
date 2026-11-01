package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 提交订单返回。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdersSubmitVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNumber;
    private BigDecimal orderAmount;
    private LocalDateTime orderTime;
}
