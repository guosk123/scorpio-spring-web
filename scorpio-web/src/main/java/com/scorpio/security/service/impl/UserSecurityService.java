package com.scorpio.security.service.impl;

import com.google.common.collect.Lists;
import com.scorpio.Constants;
import com.scorpio.security.bo.LoggedUser;
import com.scorpio.security.data.UserDO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 实现UserDetailsService，自行封装从数据库读取到的用户信息，由security去校验
 */
@Service
public class UserSecurityService implements UserDetailsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserSecurityService.class);

  /**
   * @see UserDetailsService#loadUserByUsername(String)
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    // 通过用户名查询数据库内用户信息
    UserDO userDO = new UserDO();
    userDO.setName(username);

    // 业务逻辑。。。。
    if (StringUtils.isBlank(userDO.getName())) {
      throw new UsernameNotFoundException("can not find user " + username);
    }

    // 校验用户是否被锁定等等，和用户登录相关的配置

    // 数据库获取用户权限
    List<GrantedAuthority> authorities = collectGrantedAuthorities(username);

    return new LoggedUser(userDO, authorities,
        StringUtils.equals(userDO.getNeedChangePassword(), Constants.BOOL_YES));
  }

  private List<GrantedAuthority> collectGrantedAuthorities(String username) {

    // 从角色权限关系表里收集权限
    List<GrantedAuthority> authorities = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    GrantedAuthority authority = new SimpleGrantedAuthority("SYS_USER");
    authorities.add(authority);

    return authorities;
  }

}
