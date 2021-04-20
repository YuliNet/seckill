package com.example.seckill.mappers;

import com.example.seckill.dao.SeckillInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// DAO层，定义一些增删改查操作
@Mapper
public interface SeckillMapper {

    int saveSeckillInfo(@Param("pojo") SeckillInfo seckillInfo);

    int removeSeckillInfoById(@Param("id")Long id);

    SeckillInfo findSeckillInfoById(@Param("id")Long id);

    int updateSeckillInfo(@Param("pojo")SeckillInfo seckillInfo);

    List<SeckillInfo> findAllSeckillInfo();

}
