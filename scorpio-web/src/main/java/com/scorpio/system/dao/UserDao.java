package com.scorpio.system.dao;

import com.scorpio.system.data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author guosk
 * <p>
 * create at 2022/11/9, scorpio-spring-web
 */
@Repository
public class UserDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void save(User user){

    }

}
