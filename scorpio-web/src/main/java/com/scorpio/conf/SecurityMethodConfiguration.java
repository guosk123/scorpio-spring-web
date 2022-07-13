package com.scorpio.conf;

import com.google.common.collect.Lists;
import com.scorpio.Constants;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import java.util.List;

@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityMethodConfiguration extends GlobalMethodSecurityConfiguration {

  @Override
  public AccessDecisionManager accessDecisionManager() {
    List<AccessDecisionVoter<? extends Object>> decisionVoters = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    RoleVoter roleVoter = new RoleVoter();
    roleVoter.setRolePrefix("PERM_");
    decisionVoters.add(roleVoter);
    return new AffirmativeBased(decisionVoters);
  }
}
