package com.healthcare.api_gateway.config;

import com.healthcare.api_gateway.config.properties.UserContextSigningProperties;
import com.healthcare.api_gateway.filter.signing.PemKeys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;

@Configuration
public class UserContextSigningConfig {

    @Bean
    public PrivateKey userContextPrivateKey(UserContextSigningProperties props) {

        if (props.keyId() == null || props.keyId().isBlank()) {
            throw new IllegalStateException("security.user-context.key-id is empty");
        }

        String pem = resolvePrivateKeyPem(props);
        return PemKeys.readRsaPrivateKey(pem);
    }

    private static String resolvePrivateKeyPem(UserContextSigningProperties props) {

        // prefer file path in dev/compose
        if (props.privateKeyPath() != null && !props.privateKeyPath().isBlank()) {
            try {
                return Files.readString(Path.of(props.privateKeyPath()));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read private key from path: " + props.privateKeyPath(), e);
            }
        }

        // fallback to env/config PEM
        if (props.privateKeyPem() != null && !props.privateKeyPem().isBlank()) {
            return props.privateKeyPem();
        }

        throw new IllegalStateException("No private key configured (private-key-path or private-key-pem)");
    }
}
