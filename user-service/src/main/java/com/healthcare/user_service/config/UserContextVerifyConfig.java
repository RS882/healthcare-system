package com.healthcare.user_service.config;


import com.healthcare.user_service.config.properties.UserContextVerifyProperties;
import com.healthcare.user_service.filter.security.PemPublicKeys;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;

@Configuration
@ConditionalOnProperty(
        name = "user-context-filter.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class UserContextVerifyConfig {

    @Bean
    public RSAPublicKey userContextPublicKey(UserContextVerifyProperties props) {

        String pem = resolvePublicKeyPem(props);
        return PemPublicKeys.readRsaPublicKey(pem);
    }

    private static String resolvePublicKeyPem(UserContextVerifyProperties props) {

        if (props.publicKeyPath() != null && !props.publicKeyPath().isBlank()) {
            try {
                return Files.readString(Path.of(props.publicKeyPath()));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read public key from path: " + props.publicKeyPath(), e);
            }
        }

        if (props.publicKeyPem() != null && !props.publicKeyPem().isBlank()) {
            return props.publicKeyPem();
        }

        throw new IllegalStateException("No public key configured (public-key-path or public-key-pem)");
    }
}