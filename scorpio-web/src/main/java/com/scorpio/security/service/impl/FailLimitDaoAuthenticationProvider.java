package com.scorpio.security.service.impl;

import com.scorpio.Constants;
import com.scorpio.helper.HotPropertiesHelper;
import com.scorpio.session.AuthFailedCountService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.util.function.Tuple2;

import javax.servlet.http.HttpServletRequest;

/**
 * DaoAuthenticationProvider 是security校验用户名密码匹配的逻辑，我们可以继承该类，再使用它提供的校验方法的基础上增加我们自己的业务逻辑
 */
@Service
public class FailLimitDaoAuthenticationProvider extends DaoAuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(FailLimitDaoAuthenticationProvider.class);

    // 记录连续登录失败次数和最近登录时间
    @Autowired
    private AuthFailedCountService authFailedCountService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doAfterPropertiesSet() {
        setUserDetailsService(userDetailsService);
        setPasswordEncoder(new EnhanceBCryptPasswordEncoder());
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        int fobiddenFailedTimes = Integer.parseInt(HotPropertiesHelper.getProperty("loggeduser.forbidden.max.failed"));
        int fobiddenDurationSecond = Integer.parseInt(HotPropertiesHelper.getProperty("loggeduser.forbidden.duration.second"));

        // 用户名
        String username = authentication.getName();
        String ipAddress = "";

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        if (StringUtils.isNotBlank(request.getHeader("x-forwarded-for"))
                && !StringUtils.equalsIgnoreCase("unknown", request.getHeader("x-forwarded-for"))) {
            ipAddress = StringUtils.substringBefore(request.getHeader("x-forwarded-for"), ",");
        } else {
            Object details = authentication.getDetails();
            if (details instanceof WebAuthenticationDetails) {
                WebAuthenticationDetails webDetails = (WebAuthenticationDetails) details;
                ipAddress = webDetails.getRemoteAddress();
            }
        }

        try {
            // 检查用户连续失败次数是否超过阈值
            long current = System.currentTimeMillis();
            Tuple2<Integer, Long> userFailedCount = authFailedCountService.getFailedCount(username);
            // 登录失败次数超过上限, 并且当前时间距最近一次登录失败时间间隔在锁定时间内时, 用户为锁定状态
            if (userFailedCount != null && userFailedCount.getT1() >= Integer.valueOf(fobiddenFailedTimes)
                    && current - userFailedCount.getT2() > 0
                    && current - userFailedCount.getT2() < fobiddenDurationSecond * 1000) {

                String message = "当前账户连续登录失败超过" + fobiddenFailedTimes + "次，将在"
                        + fobiddenDurationSecond / Constants.ONE_MINUTE_SECONDS + "分钟内禁止登录。";

                LOGGER.info("failed to login because user {} was locked at {}", username,
                        userFailedCount.getT2());
                // 记录用户登录失败多次后，账户被锁定日志
                throw new LockedException(message);
            }

            Authentication auth = super.authenticate(authentication);

            // 认证成功重置尝试次数
            authFailedCountService.reset(username);

            return auth;
        } catch (BadCredentialsException e) {

            // 认证不通过，记录失败次数
            Tuple2<Integer, Long> newUserFailedCount = authFailedCountService.authFail(username);
            // 记录失败日志

            throw e;
        }

    }

    public static class EnhanceBCryptPasswordEncoder extends BCryptPasswordEncoder {

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return super.matches(rawPassword, encodedPassword);
        }
    }

}
