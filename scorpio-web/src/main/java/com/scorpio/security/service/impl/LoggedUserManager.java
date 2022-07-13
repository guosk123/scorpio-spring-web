package com.scorpio.security.service.impl;

import com.scorpio.security.LoggedUserContext;
import com.scorpio.security.bo.LoggedUser;
import com.scorpio.session.SessionExpireService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoggedUserManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggedUserManager.class);

  @Autowired
  private SessionRegistry sessionRegistry;

  @Autowired
  private SessionExpireService sessionExpireService;

  @Autowired
  private UserSecurityService userSecurityService;

  public void refreshOrExpire(String sessionId, int sessionExpiredSecond) {
    long current = System.currentTimeMillis();
    if (sessionExpireService.isSessionExpire(sessionId, current, sessionExpiredSecond)) {
      LOGGER.debug(
          "start to expire user, sessionId:{}, lastOperationTime at:{}, sessionExpiredSecond:{}, current:{}",
          sessionId, sessionExpireService.getLastOperationTimestamp(sessionId),
          sessionExpiredSecond, current);

      // 踢出用户, 清掉sessionId(不清除用户所有session, 兼容一个用户可以多次登录的场景)
      forceExpireBySessionId(sessionId);

      saveLog("用户因长时间未操作，超时退出");
      throw new SessionAuthenticationException("长时间未操作, 用户登出");
    }

    if (sessionRegistry.getSessionInformation(sessionId) == null
        || sessionRegistry.getSessionInformation(sessionId).isExpired()) {
      saveLog("用户因长时间未操作，超时退出");
      throw new SessionAuthenticationException("长时间未操作, 用户登出");
    }

    sessionExpireService.refreshLastOperationTime(sessionId, current);
  }

  /**
   * 清除指定session
   * @param sessionId
   */
  public void forceExpireBySessionId(String sessionId) {
    SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionId);
    if (sessionInformation != null) {
      sessionInformation.expireNow();
      sessionExpireService.remove(sessionInformation.getSessionId());
    }
  }

  /**
   * 清除用户的所有session
   * @param username
   */
  public void forceExpireByUsername(String username) {
    UserDetails user = userSecurityService.loadUserByUsername(username);
    List<SessionInformation> sessionsList = sessionRegistry.getAllSessions(user, false);
    for (SessionInformation sessionInformation : sessionsList) {
      sessionInformation.expireNow();
      sessionExpireService.remove(sessionInformation.getSessionId());
    }
  }

  /**
   * 清除用户的所有session
   * @param userDetails
   */
  public void forceExpireByUserDetails(UserDetails userDetails) {
    List<SessionInformation> sessionsList = sessionRegistry.getAllSessions(userDetails, false);
    for (SessionInformation sessionInformation : sessionsList) {
      sessionInformation.expireNow();
      sessionExpireService.remove(sessionInformation.getSessionId());
    }
  }

  /**
   * sessionExpireMap中删除sessionId
   * 
   * @param sessionId
   */
  public void removeSession(String sessionId) {
    sessionExpireService.remove(sessionId);
  }

  private void saveLog(String content) {
    LoggedUser user = LoggedUserContext.getCurrentUser();
    String username = user.getUsername();
    String ipAddress = user.getRemoteAddress();

    // 记录日志
  }

}
