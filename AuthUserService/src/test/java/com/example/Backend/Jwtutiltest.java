package com.example.Backend;
import com.example.Backend.security.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET = "MySuperSecretKeyForJWTThatIsAtLeast32Chars!";
    private static final long EXPIRATION_MS = 86_400_000L; // 24 h

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION_MS);
    }

    // -----------------------------------------------------------------------
    // generateToken / extractUsername / extractRole
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("generateToken returns a non-blank token")
    void generateToken_returnsNonBlankToken() {
        String token = jwtUtil.generateToken("alice", "ADMIN");
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("extractUsername returns the subject that was embedded")
    void extractUsername_returnsEmbeddedSubject() {
        String token = jwtUtil.generateToken("alice", "ADMIN");
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("alice");
    }

    @Test
    @DisplayName("extractRole returns the role that was embedded")
    void extractRole_returnsEmbeddedRole() {
        String token = jwtUtil.generateToken("bob", "DEVELOPER");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("DEVELOPER");
    }

    // -----------------------------------------------------------------------
    // isTokenValid
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("isTokenValid returns true for a freshly generated token")
    void isTokenValid_trueForFreshToken() {
        String token = jwtUtil.generateToken("carol", "SCRUM_MASTER");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid returns false for a garbage string")
    void isTokenValid_falseForGarbage() {
        assertThat(jwtUtil.isTokenValid("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false for an already-expired token")
    void isTokenValid_falseForExpiredToken() {
        // negative expiration → already expired
        JwtUtil shortLived = new JwtUtil(SECRET, -1L);
        String token = shortLived.generateToken("dave", "MANAGER");
        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false for a token signed with a different secret")
    void isTokenValid_falseForWrongSecret() {
        JwtUtil otherUtil = new JwtUtil("OtherSecretKeyThatIsAlsoAtLeast32Chars!!", EXPIRATION_MS);
        String token = otherUtil.generateToken("eve", "ADMIN");
        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    // -----------------------------------------------------------------------
    // parseToken
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("parseToken returns claims with correct subject and role")
    void parseToken_returnsCorrectClaims() {
        String token = jwtUtil.generateToken("frank", "PRODUCT_OWNER");
        var claims = jwtUtil.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo("frank");
        assertThat(claims.get("role", String.class)).isEqualTo("PRODUCT_OWNER");
    }

    @Test
    @DisplayName("parseToken throws for an invalid token")
    void parseToken_throwsForInvalidToken() {
        assertThatException()
                .isThrownBy(() -> jwtUtil.parseToken("bad.token.here"));
    }
}