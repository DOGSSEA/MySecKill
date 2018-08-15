package com.sea.seckill.dao;

import com.sea.seckill.domain.OrderInfo;
import com.sea.seckill.domain.SecKillOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderDao {
    @Select("select * from seckill_order where user_id=#{userId} and goods_id=#{goodsId}")
    public SecKillOrder getSecKillOrderByUserIdGoodsId(@Param("userId")long userId, @Param("goodsId")long goodsId);

    @Insert("insert into order_info(user_id, goods_id, goods_name, goods_count, goods_price, order_channel, status, create_date)values("
            + "#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel},#{status},#{createDate} )")
    @SelectKey(keyColumn="id", keyProperty="id", resultType=long.class, before=false, statement="select last_insert_id()")
    public long insert(OrderInfo orderInfo);

    @Insert("insert into seckill_order (user_id, goods_id, order_id)values(#{userId}, #{goodsId}, #{orderId})")
    public int insertSecKillOrder(SecKillOrder secKillOrder);

    @Select("select * from order_info where id = #{orderId}")
    public OrderInfo getOrderById(@Param("orderId")long orderId);
}
