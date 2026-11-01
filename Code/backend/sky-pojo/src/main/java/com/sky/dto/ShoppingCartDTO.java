package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/** 购物车操作入参。 */
@Data
public class ShoppingCartDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long dishId;
    private Long setmealId;
    private String dishFlavor;
}
