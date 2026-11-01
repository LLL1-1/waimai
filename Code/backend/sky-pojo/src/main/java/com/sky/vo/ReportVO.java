package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/** 今日经营概览。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal turnover;
    private Long validOrderCount;
    private Long orderCount;
    private Long newUsers;
}
