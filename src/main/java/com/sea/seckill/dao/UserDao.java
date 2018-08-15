package com.sea.seckill.dao;

import com.sea.seckill.domain.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserDao {
    @Select("select * from user where id = #{id}")
    public User getById(@Param("id")long id );

    @Insert("insert into user(id, name)values(#{id}, #{name})")
    public int insert( User user );

    @Update("update seckill_user set password = #{password} where id = #{id}")
    public void update(User update);
}
