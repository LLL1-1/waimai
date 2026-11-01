package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端店铺状态接口。
 *
 * <p>用户端浏览菜品/套餐前需要先知道店铺是否营业，所以这个接口暂不要求登录。</p>
 */
@RestController
@RequestMapping("/user/shop")
@RequiredArgsConstructor
@Tag(name = "用户端店铺状态")
public class UserShopController {

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
}
