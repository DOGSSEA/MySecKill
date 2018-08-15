package com.sea.seckill.vo;

import com.sea.seckill.domain.User;

public class GoodsDetailVo {
    private int secKillStatus = 0;
    private int remainSeconds = 0;
    private GoodsVo goods ;
    private User user;

    public int getSecKillStatus() {
        return secKillStatus;
    }

    public void setSecKillStatus(int secKillStatus) {
        this.secKillStatus = secKillStatus;
    }

    public int getRemainSeconds() {
        return remainSeconds;
    }

    public void setRemainSeconds(int remainSeconds) {
        this.remainSeconds = remainSeconds;
    }

    public GoodsVo getGoods() {
        return goods;
    }

    public void setGoods(GoodsVo goods) {
        this.goods = goods;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}