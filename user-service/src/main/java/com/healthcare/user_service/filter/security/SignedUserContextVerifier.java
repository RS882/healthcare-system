package com.healthcare.user_service.filter.security;

import com.healthcare.user_service.config.properties.UserContextVerifyProperties;
import com.healthcare.user_service.filter.security.interfaces.UserContextVerifier;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;

@Service
@AllArgsConstructor
@ConditionalOnProperty(
        name = "user-context-filter.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SignedUserContextVerifier implements UserContextVerifier {

    private final UserContextVerifyProperties props;
    private final RSAPublicKey publicKey;

    private static final String VERSION_OF_SIGN = "1.0";

    @Override
    public Claims verifyAndGetClaims(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .requireIssuer(props.issuer())
                    .require("ver", VERSION_OF_SIGN)
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);

            String kid = jws.getHeader().getKeyId();
            if (props.keyId() != null && !props.keyId().isBlank() && !props.keyId().equals(kid)) {
                throw new SecurityException("Unexpected kid: " + kid);
            }

            return jws.getPayload();

        } catch (JwtException e) {
            throw new SecurityException("Invalid X-User-Context", e);
        }
    }
}