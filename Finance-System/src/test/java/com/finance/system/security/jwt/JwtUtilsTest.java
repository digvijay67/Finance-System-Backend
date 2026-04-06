package com.finance.system.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        String secret = Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes());
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000L);
        ReflectionTestUtils.setField(jwtUtils, "jwtRefreshExpirationMs", 86400000L);
    }

    @Test
    void generateAccessToken_shouldCreateTokenWithClaims() {
        String token = jwtUtils.generateAccessToken(42L, "user@finance.com", "ADMIN");

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.extractEmail(token)).isEqualTo("user@finance.com");
        assertThat(jwtUtils.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtUtils.extractRole(token)).isEqualTo("ADMIN");
        assertThat(jwtUtils.extractExpiration(token)).isNotNull();
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        assertThat(jwtUtils.validateToken("not-a-valid-jwt")).isFalse();
    }
}
