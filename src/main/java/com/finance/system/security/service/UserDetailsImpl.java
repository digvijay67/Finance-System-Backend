package com.finance.system.security.service;

import com.finance.system.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String fullName;
    private final User.Role role;
    private final User.UserStatus status;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(User user) {
        this.id         = user.getId();
        this.email      = user.getEmail();
        this.password   = user.getPassword();
        this.fullName   = user.getFullName();
        this.role       = user.getRole();
        this.status     = user.getStatus();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override public String getUsername()                  { return email; }
    @Override public String getPassword()                  { return password; }
    @Override public boolean isAccountNonExpired()         { return true; }
    @Override public boolean isAccountNonLocked()          { return status == User.UserStatus.ACTIVE; }
    @Override public boolean isCredentialsNonExpired()     { return true; }
    @Override public boolean isEnabled()                   { return status == User.UserStatus.ACTIVE; }
}
