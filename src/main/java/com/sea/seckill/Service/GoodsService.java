package com.sea.seckill.Service;

import com.sea.seckill.dao.GoodsDao;
import com.sea.seckill.domain.SecKillGoods;
import com.sea.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {
    @Autowired
    GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVo(){
        return goodsDao.listGoodsVo();
    }
    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }
    public boolean reduceStock(GoodsVo goods) {
        SecKillGoods g = new SecKillGoods();
        g.setGoodsId(goods.getId());
        return goodsDao.reduceStock(g)>0;
    }

}
