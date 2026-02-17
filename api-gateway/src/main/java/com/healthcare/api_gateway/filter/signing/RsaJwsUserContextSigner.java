package com.healthcare.api_gateway.filter.signing;

import com.healthcare.api_gateway.config.properties.UserContextSigningProperties;
import com.healthcare.api_gateway.filter.signing.interfaces.UserContextSigner;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class RsaJwsUserContextSigner implements UserContextSigner {

    private final UserContextSigningProperties props;
    private final PrivateKey privateKey;
    private final Clock clock;

    private final String VERSION_OF_SIGN = "1.0";

    public RsaJwsUserContextSigner(UserContextSigningProperties props, PrivateKey userContextPrivateKey) {
        this(props, userContextPrivateKey, Clock.systemUTC());
    }

    @Override
    public String sign(String userId, List<String> roles, String requestId, Duration ttl) {

        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId is blank");
        if (requestId == null || requestId.isBlank()) throw new IllegalArgumentException("requestId is blank");

        Duration effectiveTtl = normalizeTtl(ttl, props.defaultTtl());

        Instant now = Instant.now(clock);
        Instant exp = now.plus(effectiveTtl);

        List<String> safeRoles = (roles == null) ? List.of() : List.copyOf(roles);

        try {
            return Jwts.builder()
                    .header()
                    .keyId(props.keyId())
                    .add("typ", "JWT")
                    .and()
                    .issuer(props.issuer())
                    .subject(userId)
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(exp))
                    .claim("rid", requestId)
                    .claim("roles", safeRoles)
                    .claim("ver", VERSION_OF_SIGN)
                    .signWith(privateKey, Jwts.SIG.RS256)
                    .compact();

        } catch (SignatureException ex) {
            log.error("Failed to sign X-User-Context (RSA signature error)", ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Failed to sign X-User-Context", ex);
            throw ex;
        }
    }

    private static Duration normalizeTtl(Duration configured, Duration fallback) {
        Duration base = (fallback == null || fallback.isZero() || fallback.isNegative())
                ? Duration.ofSeconds(30)
                : fallback;

        if (configured == null || configured.isZero() || configured.isNegative()) {
            return base;
        }
        return configured;
    }
}
