package com.example.seckill.controllers;

import com.example.seckill.dao.Goods;
import com.example.seckill.dao.SeckillInfo;
import com.example.seckill.services.CommodityOrderService;
import com.example.seckill.services.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


// 不展示相应的商品，只是测试系统的性能
@Controller
public class GoodsController {

    private SeckillService seckillService;
    private CommodityOrderService commodityOrderService;

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

    @RequestMapping(value = "/pucharseGoods", method = RequestMethod.GET)
    @ResponseBody
    public String pucharseGoods(@RequestParam("goodid") Integer goodsId,
                                 @RequestParam("userid") Integer userId) {
        try {
            commodityOrderService.overSold(userId, goodsId);
        }catch (RuntimeException e) {
            e.printStackTrace();
        }
        return "success";
    }
}
