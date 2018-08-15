package com.sea.seckill.controller;

import com.sea.seckill.Service.UserService;
import com.sea.seckill.domain.User;
import com.sea.seckill.rabbitmq.MQSender;
import com.sea.seckill.redis.RedisService;
import com.sea.seckill.redis.UserKey;
import com.sea.seckill.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class SampleController {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender sender;

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet() {
         User user = userService.getById(1);
         return Result.success(user);
    }
    @RequestMapping("/db/tx")
    @ResponseBody
    public  Result<Boolean> dbTx(){

        return Result.success(true);
    }
    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet(){

       return null;
    }

/*    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet(){
        User user = new User();

        return null;
    }

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq() {
		sender.send("hello,imooc");
        return Result.success("Hello，world");
    }
  	@RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> topic() {
		sender.sendTopic("hello,imooc");
       return Result.success("Hello，world");
   }
   //swagger
	@RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> fanout() {
		sender.sendFanout("hello,imooc");
        return Result.success("Hello，world");
   }
	@RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> header() {
		sender.sendHeader("hello,imooc");
       return Result.success("Hello，world");
    }*/
}
