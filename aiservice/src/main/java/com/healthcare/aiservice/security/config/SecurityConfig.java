package com.healthcare.aiservice.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

import static com.healthcare.aiservice.common.medical_extraction.controller.API.MedicalInfoExtractionApiPaths.EXTRACT_MEDICAL_INFO_URL;
import static com.healthcare.aiservice.common.medical_summary.controller.API.MedicalSummaryApiPaths.MEDICAL_NOTE_SUMMARY_URL;
import static com.healthcare.aiservice.common.message_classification.controller.API.MessageClassificationApiPaths.CLASSIFY_MESSAGE_URL;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain configureAuth(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, MEDICAL_NOTE_SUMMARY_URL).permitAll()
                        .requestMatchers(HttpMethod.POST, CLASSIFY_MESSAGE_URL).permitAll()
                        .requestMatchers(HttpMethod.POST, EXTRACT_MEDICAL_INFO_URL).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)

                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("No local users configured");
        };
    }
}


