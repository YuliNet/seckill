package com.example.seckill.controllers;

import com.example.seckill.dao.SeckillInfo;
import com.example.seckill.services.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


// 不展示相应的商品，只是测试系统的性能
@Controller
public class GoodsController {

    private SeckillService seckillService;

    @Autowired
    public void setSeckillService(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    @RequestMapping(value = "/getGoodsInfo/{id}", method = RequestMethod.GET)
    @ResponseBody
    public SeckillInfo getseckillInfo(@PathVariable(value = "id")Long id) {
        return seckillService.findSeckillInfoById(id);
    }
}
