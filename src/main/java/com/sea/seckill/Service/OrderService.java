package com.sea.seckill.Service;

import com.sea.seckill.dao.OrderDao;
import com.sea.seckill.domain.OrderInfo;
import com.sea.seckill.domain.SecKillOrder;
import com.sea.seckill.domain.User;
import com.sea.seckill.redis.OrderKey;
import com.sea.seckill.redis.RedisService;
import com.sea.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {
    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    public SecKillOrder getSecKillOrderByUserIdGoodsId(long userId, long goodsId) {
        return redisService.get(OrderKey.getSecKillOrderByUidGid, ""+userId+"_"+goodsId, SecKillOrder.class);
    }
    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }
    @Transactional
    public OrderInfo createOrder(User user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getSecKillPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        orderDao.insert(orderInfo);
        SecKillOrder secKillOrder = new SecKillOrder();
        secKillOrder.setGoodsId(goods.getId());
        secKillOrder.setOrderId(orderInfo.getId());
        secKillOrder.setUserId(user.getId());
        orderDao.insertSecKillOrder(secKillOrder);
        redisService.set(OrderKey.getSecKillOrderByUidGid, ""+user.getId()+"_"+goods.getId(), secKillOrder);
        return orderInfo;
    }
}
