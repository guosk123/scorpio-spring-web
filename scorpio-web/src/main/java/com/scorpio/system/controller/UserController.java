package com.scorpio.system.controller;

import com.scorpio.system.data.User;
import com.scorpio.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scorpio.security.LoggedUserContext;
import com.scorpio.security.bo.LoggedUser;

@RestController
@RequestMapping("/api/user")
public class UserController {

  @Autowired
  private UserService userService;

  @GetMapping("/current-user")
  public LoggedUser getCurrentUser() {
    return LoggedUserContext.getCurrentUser();
  }

  @PostMapping("/users")
  public void saveUser(User user){
    userService.saveUser(user);
  }

}
