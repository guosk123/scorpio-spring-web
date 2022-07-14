package com.scorpio.appliance.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scorpio.security.LoggedUserContext;
import com.scorpio.security.bo.LoggedUser;

@RestController
@RequestMapping("/api/user")
public class UserController {

  @GetMapping("/current-user")
  public LoggedUser getCurrentUser() {
    return LoggedUserContext.getCurrentUser();
  }

}
