package com.example.seckill.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommodityOrderMapper {
    /**
     * 根据userid和goodsid生成订单
     * @param userId
     * @param goodsId
     * @return
     */
    int overSold(@Param("userId") Integer userId, @Param("goodsId") Integer goodsId);
}
