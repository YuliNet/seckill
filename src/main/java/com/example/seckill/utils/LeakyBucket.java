package com.example.seckill.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 用来作为桶限流的一种工具类
 * 作用类似于缓存
 */
@Slf4j
public class LeakyBucket {
    private final double rate;
    private final double capacity;
    private int storage;
    private long refreshTime;

    /**
     * 初始化桶中的速率和容量
     * @param rate
     * @param capacity
     */
    public LeakyBucket (double rate, double capacity) {
        this.rate = rate;
        this.capacity = capacity;
    }

    /**
     * 刷新桶中的流量出口
     * 注意这个是漏斗状的缓存，出口处的速率保持一致
     */
    private void refreshStorage() {
        long nowTime = System.currentTimeMillis();
        storage = (int) Math.max(0,storage - (nowTime - refreshTime) * rate);
        this.refreshTime = nowTime;
    }

    /**
     * 当用户想用漏斗缓存的时候，进行接口限流
     * @return
     */
    public synchronized boolean enterBucket () {
        refreshStorage();
        log.info("当前桶中的流量为：{}", storage);
        if(storage < capacity) {
            storage ++;
            return true;
        }
        return false;
    }
}
