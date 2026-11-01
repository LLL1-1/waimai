package com.sky.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/** 提交订单入参。 */
@Data
public class OrdersSubmitDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "地址不能为空")
    private Long addressBookId;
    private String remark;
    private Integer payMethod;
    private Integer tablewareNumber;
    private Integer tablewareStatus;
}
