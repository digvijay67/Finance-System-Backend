package com.finance.system.security.service;

import com.finance.system.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsImplTest {

    @Test
    void userDetailsImpl_shouldExposeCorrectPropertiesForActiveUser() {
        User user = User.builder()
                .id(99L)
                .email("user@example.com")
                .password("secret")
                .fullName("Example User")
                .role(User.Role.ANALYST)
                .status(User.UserStatus.ACTIVE)
                .build();

        UserDetailsImpl details = new UserDetailsImpl(user);

        assertThat(details.getId()).isEqualTo(99L);
        assertThat(details.getUsername()).isEqualTo("user@example.com");
        assertThat(details.getPassword()).isEqualTo("secret");
        assertThat(details.getFullName()).isEqualTo("Example User");
        assertThat(details.getRole()).isEqualTo(User.Role.ANALYST);
        assertThat(details.getStatus()).isEqualTo(User.UserStatus.ACTIVE);
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities()).hasSize(1);
        assertThat(details.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ANALYST");
    }

    @Test
    void userDetailsImpl_shouldDisableInactiveUser() {
        User user = User.builder()
                .id(101L)
                .email("inactive@example.com")
                .password("secret")
                .fullName("Inactive User")
                .role(User.Role.VIEWER)
                .status(User.UserStatus.INACTIVE)
                .build();

        UserDetailsImpl details = new UserDetailsImpl(user);

        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(details.isEnabled()).isFalse();
    }
}
