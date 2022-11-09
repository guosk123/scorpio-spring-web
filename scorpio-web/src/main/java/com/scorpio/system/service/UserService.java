package com.scorpio.system.service;

import com.scorpio.system.dao.UserDao;
import com.scorpio.system.data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author guosk
 * <p>
 * create at 2022/11/9, scorpio-spring-web
 */
@Service
public class UserService {
    
    @Autowired
    private UserDao userDao;

    public void saveUser(User user){
        // 密码加盐加密
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode( user.getPassword()));

        userDao.save(user);
    }

}
