package com.example.seckill.controllers;

import com.example.seckill.dao.Goods;
import com.example.seckill.dao.SeckillInfo;
import com.example.seckill.services.CommodityOrderService;
import com.example.seckill.services.SeckillService;
import com.example.seckill.utils.LeakyBucket;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


// 不展示相应的商品，只是测试系统的性能
@Slf4j
@Controller
public class GoodsController {

    private SeckillService seckillService;
    private CommodityOrderService commodityOrderService;
    private final static LeakyBucket leakyBucket = new LeakyBucket(0.01, 10);
    private static int count;
    // 创建令牌桶对象
    private final RateLimiter rateLimiter = RateLimiter.create(100);

    @Autowired
    public void setSeckillService(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    @Autowired
    public void setCommodityOrderService(CommodityOrderService commodityOrderService) {
        this.commodityOrderService = commodityOrderService;
    }

    @RequestMapping(value = "/getGoodsInfo/{id}", method = RequestMethod.GET)
    @ResponseBody
    public SeckillInfo getseckillInfo(@PathVariable(value = "id")Long id) {
        return seckillService.findSeckillInfoById(id);
    }

    /**
     * 使用悲观锁解决超卖问题
     * @param goodsId
     * @param userId
     * @return
     */
    @RequestMapping(value = "/pucharseGoods", method = RequestMethod.GET)
    @ResponseBody
    public String pucharseGoods(@RequestParam("goodid") Integer goodsId,
                                 @RequestParam("userid") Integer userId) {
        try {
            /**
             * 这里首先要进行限流，然后再加锁使用redis缓存
             * 限流使用leakybuket算法
             * 将过快的请求全部抛出
             * 经过测试发现，1000个请求只能接受45个左右
             * 说明sychronied的并发度不够大
             */
            boolean isEnterIntoBucket = leakyBucket.enterBucket();
            if(!isEnterIntoBucket)
                return "capacity is too fast, fail";
            count ++;
            log.info("请求的次数为：{}", count);
            synchronized (commodityOrderService) {
                commodityOrderService.overSold(userId, goodsId);
                return "success";
            }
        }catch (RuntimeException e) {
            // e.printStackTrace();
            log.info(e.getMessage());
            return "fail";
        }
    }

    @RequestMapping(value = "/purchaseGoodsByTokenBucket", method = RequestMethod.GET)
    @ResponseBody
    public String purchaseGoodsByTokenBucket(@RequestParam("goodid") Integer goodsId,
                                             @RequestParam("userid") Integer userId) {
        try {
            /**
             * 使用guava的桶令牌算法进行限流
             * 使用acquire方法进行自旋等待
             * 最终不会拦截请求，而是增加请求的时间
             * 从而控制速率相应所有的请求
             */
            double accquireTime = rateLimiter.acquire();
            log.info("请求的时间为：{}", accquireTime);

            /**
             * 加悲观锁防止超卖
             */
            synchronized (commodityOrderService) {
                commodityOrderService.overSold(userId, goodsId);
                return "success";
            }
        }catch (RuntimeException e) {
            // e.printStackTrace();
            log.info(e.getMessage());
            return "fail";
        }
    }
}
