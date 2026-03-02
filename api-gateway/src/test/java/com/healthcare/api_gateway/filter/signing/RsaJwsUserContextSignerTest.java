package com.healthcare.api_gateway.filter.signing;

import com.healthcare.api_gateway.config.properties.UserContextSigningProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.healthcare.api_gateway.support.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Rsa Jws UserContext Signer tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RsaJwsUserContextSignerTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-02-25T19:00:00Z");

    private static KeyPair rsaKeyPair() {
        return Jwts.SIG.RS256.keyPair().build();
    }

    private static UserContextSigningProperties props(Duration defaultTtl) {
        return new UserContextSigningProperties(
                "healthcare-gateway",
                "kid-42",
                null,
                null,
                defaultTtl
        );
    }

    private static Claims parseClaims(String jws, KeyPair kp, Instant now) {
        return Jwts.parser()
                .clock(() -> Date.from(now))
                .verifyWith(kp.getPublic())
                .build()
                .parseSignedClaims(jws)
                .getPayload();
    }

    private static Map<String, Object> parseHeader(String jws, KeyPair kp, Instant now) {
        return Jwts.parser()
                .clock(() -> Date.from(now))
                .verifyWith(kp.getPublic())
                .build()
                .parseSignedClaims(jws)
                .getHeader();
    }

    @Test
    void sign_should_throw_when_user_id_null_or_blank() {
        var kp = rsaKeyPair();
        var clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        var signer = new RsaJwsUserContextSigner(props(Duration.ofSeconds(60)), kp.getPrivate(), clock);

        String rid = requestId();
        List<String> roles = List.of(roleUser());
        Duration ttl = Duration.ofSeconds(10);

        String exceptionMsg = "userId is blank";

        assertThatThrownBy(() -> signer.sign(null, roles, rid, ttl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(exceptionMsg);

        assertThatThrownBy(() -> signer.sign("   ", roles, rid, ttl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(exceptionMsg);
    }

    @Test
    void sign_should_throw_when_request_id_null_or_blank() {
        var kp = rsaKeyPair();
        var clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        var signer = new RsaJwsUserContextSigner(props(Duration.ofSeconds(60)), kp.getPrivate(), clock);

        String userId = randomUserId().toString();
        List<String> roles = List.of(roleUser());
        Duration ttl = Duration.ofSeconds(10);

        String exceptionMsg = "requestId is blank";

        assertThatThrownBy(() -> signer.sign(userId, roles, null, ttl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(exceptionMsg);

        assertThatThrownBy(() -> signer.sign(userId, roles, "   ", ttl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(exceptionMsg);
    }

    @Test
    void sign_should_build_valid_jwt_with_expected_header_and_claims() {
        var kp = rsaKeyPair();
        var clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        var signer = new RsaJwsUserContextSigner(props(Duration.ofSeconds(120)), kp.getPrivate(), clock);

        String userId = randomUserId().toString();
        String rid = requestId();
        List<String> roles = List.of(roleUser(), roleAdmin());
        Duration ttl = Duration.ofSeconds(10);

        String token = signer.sign(userId, roles, rid, ttl);

        Map<String, Object> header = parseHeader(token, kp, FIXED_NOW);
        Claims claims = parseClaims(token, kp, FIXED_NOW);

        // header
        assertThat(header.get("kid")).isEqualTo("kid-42");
        assertThat(header.get("typ")).isEqualTo("JWT");

        // std claims
        assertThat(claims.getIssuer()).isEqualTo("healthcare-gateway");
        assertThat(claims.getSubject()).isEqualTo(userId);
        assertThat(claims.getIssuedAt().toInstant()).isEqualTo(FIXED_NOW);
        assertThat(claims.getExpiration().toInstant()).isEqualTo(FIXED_NOW.plusSeconds(10));

        // custom claims
        assertThat(claims.get("rid", String.class)).isEqualTo(rid);
        assertThat(claims.get("roles")).isEqualTo(roles);
        assertThat(claims.get("ver", String.class)).isEqualTo("1.0");
    }

    @Test
    void sign_should_use_props_default_ttl_when_ttl_null_or_zero_or_negative() {
        var kp = rsaKeyPair();
        var clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        var signer = new RsaJwsUserContextSigner(props(Duration.ofSeconds(90)), kp.getPrivate(), clock);

        String userId = randomUserId().toString();
        String rid = requestId();
        List<String> roles = List.of(roleAdmin());

        String t1 = signer.sign(userId, roles, rid, null);
        String t2 = signer.sign(userId, roles, rid, Duration.ZERO);
        String t3 = signer.sign(userId, roles, rid, Duration.ofSeconds(-5));

        assertThat(parseClaims(t1, kp, FIXED_NOW).getExpiration().toInstant()).isEqualTo(FIXED_NOW.plusSeconds(90));
        assertThat(parseClaims(t2, kp, FIXED_NOW).getExpiration().toInstant()).isEqualTo(FIXED_NOW.plusSeconds(90));
        assertThat(parseClaims(t3, kp, FIXED_NOW).getExpiration().toInstant()).isEqualTo(FIXED_NOW.plusSeconds(90));
    }

    @Test
    void sign_should_fallback_to_30_seconds_when_props_default_ttl_invalid() {

        var kp = rsaKeyPair();
        var clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);

        var signer = new RsaJwsUserContextSigner(props(Duration.ZERO), kp.getPrivate(), clock);

        String userId = randomUserId().toString();
        String rid = requestId();

        String token = signer.sign(userId, null, rid, null);
        Claims claims = parseClaims(token, kp, FIXED_NOW);

        assertThat(claims.getExpiration().toInstant()).isEqualTo(FIXED_NOW.plusSeconds(30));
        assertThat(claims.get("roles")).isEqualTo(List.of());
    }

    @Test
    void sign_should_not_be_verifiable_with_other_public_key() {
        var kp1 = rsaKeyPair();
        var kp2 = rsaKeyPair();

        var clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        var signer = new RsaJwsUserContextSigner(props(Duration.ofSeconds(60)), kp1.getPrivate(), clock);

        String userId = randomUserId().toString();
        String rid = requestId();
        List<String> roles = List.of(roleUser(), roleAdmin());
        Duration ttl = Duration.ofSeconds(7);

        String token = signer.sign(userId, roles, rid, ttl);

        assertThatThrownBy(() ->
                Jwts.parser().verifyWith(kp2.getPublic()).build().parseSignedClaims(token)
        ).isInstanceOf(io.jsonwebtoken.security.SignatureException.class);
    }
}