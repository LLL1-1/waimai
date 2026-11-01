package com.sky.controller.admin;

import com.sky.dto.OrderPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.vo.OrdersVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 管理端订单接口。 */
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
@Tag(name = "订单管理")
public class AdminOrderController {

    private final OrdersService ordersService;

    @GetMapping("/page")
    @Operation(summary = "订单分页")
    public Result<PageResult<Orders>> page(OrderPageQueryDTO orderPageQueryDTO) {
        return Result.success(ordersService.adminPage(orderPageQueryDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public Result<OrdersVO> detail(@PathVariable Long id) {
        return Result.success(ordersService.adminDetail(id));
    }

    @PutMapping("/confirm/{id}")
    @Operation(summary = "接单")
    public Result<Void> confirm(@PathVariable Long id) {
        ordersService.confirm(id);
        return Result.success();
    }

    @PutMapping("/reject/{id}")
    @Operation(summary = "拒单")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestParam(required = false) String rejectionReason) {
        ordersService.reject(id, rejectionReason);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @Operation(summary = "派送")
    public Result<Void> delivery(@PathVariable Long id) {
        ordersService.delivery(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @Operation(summary = "完成订单")
    public Result<Void> complete(@PathVariable Long id) {
        ordersService.complete(id);
        return Result.success();
    }
}
