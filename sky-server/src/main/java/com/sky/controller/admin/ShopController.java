package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺相关接口
 */
@Slf4j
@Api(tags = "店铺相关接口")
@RestController("adminShopController")
@RequestMapping("/admin/shop")
public class ShopController {



    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 设置店铺的营业状态
     * @param status
     * @return
     */
    @ApiOperation("设置店铺的营业状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置店铺的营业状态：{}", status == 1 ? "营业中" : "打烊中");
        stringRedisTemplate.opsForValue().set("SHOP_STATUS", status.toString());
        return Result.success();
    }

    /**
     * 获取店铺营业状态
     * @return
     */
    @ApiOperation("获取店铺营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        String shopStatus = stringRedisTemplate.opsForValue().get("SHOP_STATUS");
        Integer status = 0;
        if (shopStatus == null) {
            stringRedisTemplate.opsForValue().set("SHOP_STATUS", String.valueOf(0));
        } else {
            status = Integer.valueOf(shopStatus);
        }
        log.info("获取店铺营业状态：{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
