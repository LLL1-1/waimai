package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端店铺状态接口。
 *
 * <p>营业状态存在 Redis，管理端可以查询和切换。</p>
 */
@RestController
@RequestMapping("/admin/shop")
@RequiredArgsConstructor
@Tag(name = "店铺状态")
public class AdminShopController {

    private final ShopService shopService;

    /**
     * 查询营业状态。
     *
     * @return 1 营业中，0 打烊
     */
    @GetMapping("/status")
    @Operation(summary = "查询店铺营业状态")
    public Result<Integer> getStatus() {
        return Result.success(shopService.getStatus());
    }

    /**
     * 设置营业状态。
     *
     * @param status 1 营业中，0 打烊
     * @return 成功响应
     */
    @PutMapping("/{status}")
    @Operation(summary = "设置店铺营业状态")
    public Result<Void> setStatus(@PathVariable Integer status) {
        shopService.setStatus(status);
        return Result.success();
    }
}
