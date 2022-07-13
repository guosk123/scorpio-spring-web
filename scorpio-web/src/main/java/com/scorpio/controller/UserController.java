package com.scorpio.controller;

import com.scorpio.security.LoggedUserContext;
import com.scorpio.security.bo.LoggedUser;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @RequestMapping("/current-user")
    @Secured("PERM_SYS_USER")
    public LoggedUser getCurrentUser(){
        return LoggedUserContext.getCurrentUser();
    }

}
