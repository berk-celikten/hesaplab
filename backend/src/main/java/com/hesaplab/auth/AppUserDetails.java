package com.hesaplab.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AppUserDetails implements UserDetails {
    private final UserDto user;
    private final String passwordHash;

    public AppUserDetails(UserDto user, String passwordHash) {
        this.user = user;
        this.passwordHash = passwordHash;
    }
    public UserDto user() { return user; }
    @Override
    public String getUsername() { return user.username(); }
    @Override
    @JsonIgnore
    public String getPassword() { return passwordHash; }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.role()));
    }
}
