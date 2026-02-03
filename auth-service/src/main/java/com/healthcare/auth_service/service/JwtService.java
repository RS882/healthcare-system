package com.healthcare.auth_service.service;

import com.healthcare.auth_service.config.properties.JwtProperties;
import com.healthcare.auth_service.domain.dto.TokensDto;
import com.healthcare.auth_service.exception_handler.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtProperties jwtProps;

    private static final String TOKENS_ISSUER = "Healthcare Authorization";

    private SecretKey accessKey;
    private SecretKey refreshKey;

    private final String ROLES = "roles";
    private final String USER_ID = "userId";

    @PostConstruct
    public void initKey() {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProps.accessSecret()));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProps.refreshSecret()));
    }

    public TokensDto getTokens(UserDetails userDetails, Long userId) {
        return new TokensDto(
                generateAccessToken(userDetails, userId),
                generateRefreshToken(userDetails),
                userId);
    }

    private String generateAccessToken(UserDetails userDetails, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLES, userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        claims.put(USER_ID, userId);
        long expirationMs = jwtProps.accessTokenExpiration().toMillis();
        return buildToken(claims, userDetails.getUsername(), expirationMs, accessKey);
    }

    private String generateRefreshToken(UserDetails userDetails) {
        long expirationMs = jwtProps.refreshTokenExpiration().toMillis();
        return buildToken(new HashMap<>(), userDetails.getUsername(), expirationMs, refreshKey);
    }

    private String buildToken(
            Map<String, Object> claims,
            String subject,
            long expiration,
            SecretKey key) {

        return Jwts.builder()
                .claims(claims)
                .issuer(TOKENS_ISSUER)
                .issuedAt(Date.from(Instant.now()))
                .subject(subject)
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public boolean validateAccessToken(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, accessKey);
    }

    public boolean validateRefreshToken(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, refreshKey);
    }

    private boolean isTokenValid(String token, UserDetails userDetails, SecretKey key) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            final String username = extractUserEmail(token, key);
            return username.equals(userDetails.getUsername());
        } catch (Exception e) {
            return false;
        }
    }

    public long getRemainingTTLAccessToken(String token) {
        return getRemainingTTL(token, accessKey);
    }

    public long getRemainingTTLRefreshToken(String token) {
        return getRemainingTTL(token, refreshKey);
    }

    private long getRemainingTTL(String token, SecretKey key) {
        Claims claims = extractAllClaims(token, key);
        Date expiration = claims.getExpiration();
        long now = System.currentTimeMillis();
        return Math.max(expiration.getTime() - now, 0);
    }

    public String extractUserEmailFromAccessToken(String token) {
        return extractUserEmail(token, accessKey);
    }

    public String extractUserEmailFromRefreshToken(String token) {
        return extractUserEmail(token, refreshKey);
    }

    private String extractUserEmail(String token, SecretKey key) {
        return extractClaim(token, Claims::getSubject, key);
    }

    public Long extractUserIdFromAccessToken(String token) {
        Claims claims = extractAllClaims(token, accessKey);
        return claims.get(USER_ID, Long.class);
    }

    public List<String> extractRolesFromAccessToken(String token) {
        Claims claims = extractAllClaims(token, accessKey);
        return claims.get(ROLES, List.class);
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver, SecretKey key) {
        final Claims claims = extractAllClaims(token, key);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token, SecretKey key) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new UnauthorizedException("Token is invalid", e);
        }
    }
}