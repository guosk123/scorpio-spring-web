package com.scorpio.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.scorpio.security.LoggedUserContext;
import com.scorpio.security.bo.LoggedUser;
import com.scorpio.security.service.impl.UserSecurityService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

@RestController
@RequestMapping("/api/sso")
public class SsoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SsoController.class);

    @Autowired
    private UserSecurityService userSecurityService;

    @Autowired
    private SessionRegistry sessionRegistry;

    /*
         OEM产品时，我们需要做到用户集中管理，单个用户可登录到各个系统上去，这需要子系统能够提供接口，用户拿着token等信息，就能跳转登录到子系统

         jwt 一种签名机制
    * */
    public void ssoLogin(@RequestParam(value = "jwt", required = true) String jwt, HttpServletRequest httpRequest, HttpServletResponse httpResponse){
        // 从JWT中解析数据
         String token = "";
        try {
            token = decodeJwt(jwt);
        } catch (JWTVerificationException e) {
            LOGGER.warn("jwt decode failed.", e);
            try {
                httpResponse.sendRedirect("/error?error="
                        + URLEncoder.encode("jwt解析失败，请检查单点登录配置", "utf-8"));
            } catch (IOException e1) {
                LOGGER.warn("redirect page failed.", e1);
            }
            return;
        }

        // 从数据库读取token
        String dbToken = "";

        if(StringUtils.equals(token, dbToken)){
            //
        }

        // 校验JWT签名
        if (!verityJwt(jwt, dbToken)) {
            // 签名有误
            LOGGER.warn("jwt signature is incorrect.");
            try {
                httpResponse.sendRedirect("/error?error=" + URLEncoder.encode("验签失败，请检查单点登录配置", "utf-8"));
            } catch (IOException e1) {
                LOGGER.warn("redirect page failed.", e1);
            }
            return;
        }

        // 校验通过，自动登录
        innerLogin(token, httpRequest);
    }

    /**
     * 解析JWT获取数据
     * @param jwt
     * @return
     */
    private String decodeJwt(String jwt) {
        try {
            String token = JWT.decode(jwt).getClaim("token").asString();

            if (StringUtils.isBlank(token)) {
                LOGGER.warn("jwt payload param [platform_id] or [platform_user_id] must not be blank.");
                return null;
            }

            return token;
        } catch (JWTVerificationException jwte) {
            throw jwte;
        }
    }

    /**
     * 校验JWT
     * @param token
     * @param secret
     * @return
     */
    private boolean verityJwt(String token, String secret) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException jwte) {
            LOGGER.warn("sso jwt verification failed.", jwte);
            return false;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("sso jwt verification failed.", e);
            return false;
        }
    }

    /**
     * 注册用户信息
     * @param username
     * @param httpRequest
     */
    private void innerLogin(String username, HttpServletRequest httpRequest) {
        // 将用户信息放入SecurityContext,并注册session
        UserDetails userDetails = userSecurityService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, "", userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetails(httpRequest));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        httpRequest.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        sessionRegistry.registerNewSession(httpRequest.getSession().getId(), userDetails);

        // 记录登录日志
        LoggedUser user = LoggedUserContext.getCurrentUser();
    }

}
