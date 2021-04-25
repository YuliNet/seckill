package com.example.seckill.services;

import com.example.seckill.dao.Goods;

public interface GoodsService {
    int updateSaleNoOptimisticLock(Goods goods);
    Goods findGoodsById(Integer id);
}
