package com.scorpio.security.listener;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.scorpio.security.service.impl.LoggedUserManager;

@WebListener
public class SessionListener implements HttpSessionListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(SessionListener.class);

  @Autowired
  private LoggedUserManager loggedUserManager;

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    LOGGER.debug("session create.", event);
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    LOGGER.debug("session destroyed.", event);

    loggedUserManager.removeSession(event.getSession().getId());
  }

}
