package com.sky.controller.user;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.vo.OrdersSubmitVO;
import com.sky.vo.OrdersVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 用户端订单接口。 */
@RestController
@RequestMapping("/user/order")
@RequiredArgsConstructor
@Tag(name = "用户端订单")
public class UserOrderController {

    private final OrdersService ordersService;

    @PostMapping("/submit")
    @Operation(summary = "提交订单")
    public Result<OrdersSubmitVO> submit(@RequestBody @Valid OrdersSubmitDTO ordersSubmitDTO) {
        return Result.success(ordersService.submit(ordersSubmitDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "我的订单")
    public Result<PageResult<Orders>> page(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) Integer status) {
        return Result.success(ordersService.pageQuery(status, page, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public Result<OrdersVO> detail(@PathVariable Long id) {
        return Result.success(ordersService.detail(id));
    }

    @PutMapping("/payment/{id}")
    @Operation(summary = "模拟支付")
    public Result<Void> pay(@PathVariable Long id) {
        ordersService.pay(id);
        return Result.success();
    }

    @PutMapping("/cancel/{id}")
    @Operation(summary = "取消订单")
    public Result<Void> cancel(@PathVariable Long id) {
        ordersService.cancel(id);
        return Result.success();
    }
}
