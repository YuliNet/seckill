package com.example.seckill.services.serviceimpls;

import com.example.seckill.dao.CommodityOrder;
import com.example.seckill.dao.Goods;
import com.example.seckill.mappers.CommodityOrderMapper;
import com.example.seckill.mappers.GoodsMapper;
import com.example.seckill.services.CommodityOrderService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
public class CommodityOrderServiceImpl implements CommodityOrderService {

    @Resource
    private  CommodityOrderMapper commodityOrderMapper;

    @Resource
    private GoodsMapper goodsMapper;


    /**
     * 这里面要做特殊判断
     * 如果还有库存才能向数据库里更新数据
     * 否则就不能更新数据
     * @param userId
     * @param goodsId
     * @return
     */
    @Caching(evict = {@CacheEvict(cacheNames = "goodsList", allEntries = true)},
            put = {@CachePut(cacheNames = "good", key = "#goodsId")})
    @Override
    public int overSold(Integer userId, Integer goodsId) {
        // 首先检查当前库存中的物品是否还能够售出
        // 如果不能就抛出异常，并显示物品已经售空
        Goods goods = checkInventory(goodsId);
        // 然后更新库存信息
        deductInventory(goods);
        // 最后生成订单
        return generateOrders(userId, goodsId);
    }

    private Goods checkInventory(Integer goodsId) {
        Goods goods = goodsMapper.findGoodsById(goodsId);
        if (goods.getSale() >= goods.getStock()) {
            throw new RuntimeException(goods.getName() + "已经售空！！");
        }
        return goods;
    }

    private void deductInventory(Goods goods) {
        int updateRows = goodsMapper.updateSaleNoOptimisticLock(goods);
        if (updateRows == 0) {
            throw new RuntimeException("抢购失败，商品已经售空，请重试！");
        }
    }

    private Integer generateOrders(Integer userId, Integer goodsId) {
        int row = commodityOrderMapper.overSold(userId, goodsId);
        if (row == 0) {
            throw new RuntimeException("生成订单失败！！");
        }
        return row;
    }
}
