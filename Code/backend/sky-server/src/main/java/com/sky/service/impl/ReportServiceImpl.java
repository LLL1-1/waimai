package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.ReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 今日经营概览实现。 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrdersMapper ordersMapper;
    private final UserMapper userMapper;

    @Override
    public ReportVO today() {
        LocalDateTime start = LocalDate.now().atStartOfDay();

        Long validOrderCount = ordersMapper.selectCount(new QueryWrapper<Orders>()
                .eq("status", 5).ge("order_time", start));
        Long orderCount = ordersMapper.selectCount(new QueryWrapper<Orders>()
                .ge("order_time", start));
        Long newUsers = userMapper.selectCount(new QueryWrapper<User>()
                .ge("create_time", start));

        List<Object> sums = ordersMapper.selectObjs(new QueryWrapper<Orders>()
                .select("COALESCE(SUM(amount), 0)")
                .eq("status", 5)
                .ge("order_time", start));
        BigDecimal turnover = sums.isEmpty() || sums.get(0) == null
                ? BigDecimal.ZERO
                : new BigDecimal(sums.get(0).toString());

        return new ReportVO(turnover, validOrderCount, orderCount, newUsers);
    }
}
