package com.sea.seckill.controller;


import com.sea.seckill.Access.AccessLimit;
import com.sea.seckill.Service.GoodsService;
import com.sea.seckill.Service.OrderService;
import com.sea.seckill.Service.SecKillService;
import com.sea.seckill.domain.OrderInfo;
import com.sea.seckill.domain.SecKillOrder;
import com.sea.seckill.domain.User;
import com.sea.seckill.rabbitmq.MQSender;
import com.sea.seckill.redis.GoodsKey;
import com.sea.seckill.redis.RedisService;
import com.sea.seckill.redis.SecKillMessage;
import com.sea.seckill.result.CodeMsg;
import com.sea.seckill.result.Result;
import com.sea.seckill.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean{

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SecKillService secKillService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender sender;


    private HashMap<Long, Boolean> localOverMap =  new HashMap<Long, Boolean>();

    /**
     *  GET POST有什么区别？
     *  幂等
     * */
    @RequestMapping(value = "/{path}/do_seckill" ,method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> secKill(Model model, User user,
                       @RequestParam("goodsId")long goodsId,@PathVariable("path") String path) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", user);
        //验证path
        boolean check = secKillService.checkPath(user, goodsId, path);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if(over) {
            return Result.error(CodeMsg.SEC_KILL_OVER);
        }

        //预减库存
        long stock = redisService.decr(GoodsKey.getSecKillGoodsStock, ""+goodsId);//10
        if(stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.SEC_KILL_OVER);
        }
        //判断是否已经秒杀到了
        SecKillOrder order = orderService.getSecKillOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
            return Result.error(CodeMsg.REPEATE_SEC_KILL);
        }
        //入队
        SecKillMessage mm = new SecKillMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        sender.sendSecKillMessage(mm);
        return Result.success(0);//排队中

/*        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock <= 0) {
            model.addAttribute("errmsg", CodeMsg.SEC_KILL_OVER.getMsg());
            return Result.error(CodeMsg.SEC_KILL_OVER);
        }
        //判断是否已经秒杀到了
        SecKillOrder order = orderService.getSecKillOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
            model.addAttribute("errmsg", CodeMsg.REPEATE_SEC_KILL.getMsg());
            return Result.error(CodeMsg.REPEATE_SEC_KILL);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = secKillService.secKill(user, goods);
        if( orderInfo == null ) return Result.error(CodeMsg.BUY_FAIL);
        return Result.success(orderInfo);*/
    }

    /**
     * 系统初始化
     * */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if(goodsList == null) {
            return;
        }
        for(GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getSecKillGoodsStock, ""+goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        }
    }

    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     * */
    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public Result<Long> secKillResult(Model model,User user,
                                      @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result  =secKillService.getSecKillResult(user.getId(), goodsId);
        return Result.success(result);
    }


    @AccessLimit(seconds=5, maxCount=5, needLogin=true)
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getSecKillPath(HttpServletRequest request, User user,
                                         @RequestParam("goodsId")long goodsId,
                                         @RequestParam(value="verifyCode", defaultValue="0")int verifyCode
    ) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = secKillService.checkVerifyCode(user, goodsId, verifyCode);
        if(!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path  =secKillService.createSecKillPath(user, goodsId);
        return Result.success(path);
    }

    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCod(HttpServletResponse response, User user,
                                              @RequestParam("goodsId")long goodsId) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            BufferedImage image  = secKillService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SecKill_FAIL);
        }
    }
}
