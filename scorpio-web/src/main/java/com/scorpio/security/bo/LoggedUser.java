package com.scorpio.security.bo;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.scorpio.security.data.UserDO;
import com.scorpio.util.TextUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Collections;

public class LoggedUser extends User {

    private static final long serialVersionUID = 5993294740742671459L;

    private String fullname;
    private String appKey;
    private String appToken;
    private String remoteAddress;

    private boolean userPasswordNonReliable;

    private boolean external;

    public static final LoggedUser EMPTY = new LoggedUser();

    private LoggedUser() {
        super("_invalid_", "_invalid_", Collections.emptyList());
    }

    public LoggedUser(UserDO userDo,
                      Collection<? extends GrantedAuthority> authorities, boolean userPasswordNonReliable) {

        super(userDo.getName(), userDo.getPassword(), true, true, true,
                !TextUtils.toBoolean(userDo.getLocked()), authorities);

        this.fullname = userDo.getFullname();
        this.appKey = userDo.getAppKey();
        this.appToken = userDo.getAppToken();
        this.userPasswordNonReliable = userPasswordNonReliable;
    }

    public boolean hasAnyAuthorities(String... auths) {
        for (String auth : auths) {
            boolean anyEqual = Iterables.any(getAuthorities(), new Predicate<GrantedAuthority>() {
                @Override
                public boolean apply(GrantedAuthority grantedAuthority) {
                    return Objects.equal(grantedAuthority.getAuthority(), auth);
                }
            });
            if (anyEqual) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "LoggedUser [fullname=" + fullname + ", appKey="
                + appKey + ", remoteAddress=" + remoteAddress + ", toString()="
                + super.toString() + "]";
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppToken() {
        return appToken;
    }

    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public boolean getUserPasswordNonReliable() {
        return userPasswordNonReliable;
    }

    public void setUserPasswordNonReliable(boolean userPasswordNonReliable) {
        this.userPasswordNonReliable = userPasswordNonReliable;
    }

    public boolean getExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

}
