package com.example.seckill.services.serviceimpls;

import com.example.seckill.dao.Goods;
import com.example.seckill.mappers.GoodsMapper;
import com.example.seckill.services.GoodsService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private GoodsMapper goodsMapper;

    @Override
    @Caching(evict = {@CacheEvict(cacheNames = "goodsList", allEntries = true)},
            put = {@CachePut(cacheNames = "goods", key = "#goods.id")})
    public int updateSaleNoOptimisticLock(Goods goods) {
        return goodsMapper.updateSaleNoOptimisticLock(goods);
    }

    @Override
    @Cacheable(value = "goods")
    public Goods findGoodsById(Integer id) {
        return goodsMapper.findGoodsById(id);
    }


}
