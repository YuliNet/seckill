package com.example.seckill.dao;

import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Repository
public class SeckillInfo implements Serializable {
    private static final long serialVersionUID = -5164403560799812743L;
    private Long id;
    private String deid;
    private BigDecimal secPrice;
    private Integer stockNum;
    private Date startTime;
    private Date endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeid() {
        return deid;
    }

    public void setDeid(String deid) {
        this.deid = deid;
    }

    public BigDecimal getSecPrice() {
        return secPrice;
    }

    public void setSecPrice(BigDecimal secPrice) {
        this.secPrice = secPrice;
    }

    public Integer getStockNum() {
        return stockNum;
    }

    public void setStockNum(Integer stockNum) {
        this.stockNum = stockNum;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
