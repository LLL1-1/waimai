package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/** 订单分页查询入参。 */
@Data
public class OrderPageQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer page = 1;
    private Integer pageSize = 10;
    private Integer status;
    private String number;
}
