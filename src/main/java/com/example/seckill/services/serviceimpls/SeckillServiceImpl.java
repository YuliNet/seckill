package com.example.seckill.services.serviceimpls;

import com.example.seckill.dao.SeckillInfo;
import com.example.seckill.mappers.SeckillMapper;
import com.example.seckill.services.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Service层主要定义具体的缓存操作提供给Controller调用
 */

@Service
public class SeckillServiceImpl implements SeckillService {

    @Resource
    private SeckillMapper seckillMapper;

    /**
     * 这里要将物品存入缓存当中，之前要将缓存全部清除
     * @param seckillInfo
     * @return
     */
    @Override
    @Caching(evict = {@CacheEvict(cacheNames = "goodsList", allEntries = true)},
             put = {@CachePut(cacheNames = "good", key = "#seckillInfo.id")})
    public int saveSeckillInfo(SeckillInfo seckillInfo) {
        return seckillMapper.saveSeckillInfo(seckillInfo);
    }

    @Override
    @Caching(evict = {@CacheEvict(cacheNames = "goodsList", allEntries = true),
                      @CacheEvict(cacheNames = "goods", key = "#id")})
    public int removeSeckillInfoById(Long id) {
        return seckillMapper.removeSeckillInfoById(id);
    }

    /**
     * 这里定义缓存中的每一项存储的类型
     * @param id
     * @return
     */
    @Override
    @Cacheable(value = "goods")
    public SeckillInfo findSeckillInfoById(Long id) {
        return seckillMapper.findSeckillInfoById(id);
    }

    @Override
    @Caching(evict = {@CacheEvict(cacheNames = "goodsList", allEntries = true)},
             put = {@CachePut(cacheNames = "goods", key = "#seckillInfo.id")})
    public int updateSeckillInfo(SeckillInfo seckillInfo) {
        return seckillMapper.updateSeckillInfo(seckillInfo);
    }

    /**
     * 这里定义缓存组的类型
     * @return
     */
    @Override
    @Cacheable(value = "goodsList")
    public List<SeckillInfo> findAllSeckillInfo() {
        return seckillMapper.findAllSeckillInfo();
    }
}
