package com.example.seckill.services;

import com.example.seckill.dao.SeckillInfo;

import java.util.List;

public interface SeckillService {

    int saveSeckillInfo(SeckillInfo seckillInfo);

    int removeSeckillInfoById(Long id);

    SeckillInfo findSeckillInfoById(Long id);

    int updateSeckillInfo(SeckillInfo seckillInfo);

    List<SeckillInfo> findAllSeckillInfo();

}
