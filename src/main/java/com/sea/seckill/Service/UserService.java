package com.sea.seckill.Service;

import com.sea.seckill.dao.UserDao;
import com.sea.seckill.domain.User;
import com.sea.seckill.exception.GlobalException;
import com.sea.seckill.redis.RedisService;
import com.sea.seckill.redis.UserKey;
import com.sea.seckill.result.CodeMsg;
import com.sea.seckill.util.MD5Util;
import com.sea.seckill.util.UUIDUtil;
import com.sea.seckill.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    RedisService redisService;

    public static final String COOKIE_NAME_TOKEN = "token";

    public String login(HttpServletResponse response , LoginVo loginVo){
        if(loginVo == null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        User user = getById(Long.parseLong(mobile));
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calPass = MD5Util.formPassToDBPass(formPass,saltDB);
        if( !calPass.equals(dbPass) ){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        String token = UUIDUtil.uuid();
        addCookie(response,token,user);
        return token;
    }
    public User getByToken(HttpServletResponse response, String token) {
        if(StringUtils.isEmpty(token)) {
            return null;
        }
        User user = redisService.get(UserKey.token,token,User.class);
        //延长有效期
        if(user != null) {
            addCookie(response, token, user);
        }
        return user;
    }

    public User getById(long id) {
        //取缓存
        User user = redisService.get(UserKey.getById, ""+id, User.class);
        if(user != null) {
            return user;
        }
        //取数据库
        user = userDao.getById(id);
        if(user != null) {
            redisService.set(UserKey.getById, ""+id, user);
        }
        return user;
    }
    public boolean updatePassword(String token, long id, String formPass) {
        //取user
        User user = getById(id);
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        User update = new User();
        update.setId(id);
        update.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
        userDao.update(update);
        //处理缓存
        redisService.delete(UserKey.getById, ""+id);
        user.setPassword(update.getPassword());
        //更新token
        redisService.set(UserKey.token, token, user);
        return true;
    }

    private void addCookie(HttpServletResponse response, String token, User user) {
        redisService.set(UserKey.token,token,user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(UserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
