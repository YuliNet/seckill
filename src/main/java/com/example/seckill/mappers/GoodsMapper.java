package com.example.seckill.mappers;

import com.example.seckill.dao.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GoodsMapper {

    /**
     * 更新货物的信息
     * @param goods
     * @return
     */
    int updateSaleNoOptimisticLock(@Param("goods") Goods goods);
    Goods findGoodsById(Integer goodsId);
}
