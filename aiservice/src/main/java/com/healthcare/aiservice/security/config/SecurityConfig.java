package com.healthcare.aiservice.security.config;

import com.healthcare.aiservice.security.config.configs_components.CustomAccessDeniedHandler;
import com.healthcare.aiservice.security.config.configs_components.CustomAuthenticationEntryPoint;
import com.healthcare.aiservice.security.filter.AuthFilter;
import com.healthcare.aiservice.security.filter.RequestIdFilter;
import com.healthcare.aiservice.security.filter.UserContextFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.healthcare.aiservice.common.medical_extraction.controller.API.MedicalInfoExtractionApiPaths.EXTRACT_MEDICAL_INFO_URL;
import static com.healthcare.aiservice.common.medical_summary.controller.API.MedicalSummaryApiPaths.MEDICAL_NOTE_SUMMARY_URL;
import static com.healthcare.aiservice.common.message_classification.controller.API.MessageClassificationApiPaths.CLASSIFY_MESSAGE_URL;
import static com.healthcare.aiservice.common.prompt.controller.API.AiPromptApiPaths.*;
import static com.healthcare.aiservice.common.statistics.controller.API.AiStatisticsApiPaths.STATISTICS_ADMIN_URL;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectProvider<RequestIdFilter> requestIdFilterProvider;
    private final ObjectProvider<UserContextFilter> userContextFilterProvider;
    private final ObjectProvider<AuthFilter> authFilterProvider;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain configureAuth(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                SecurityPublicEndpoints.PUBLIC_ENDPOINTS
                        ).permitAll()

                        .requestMatchers(
                                EndpointRequest.to(HealthEndpoint.class)
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, MEDICAL_NOTE_SUMMARY_URL).permitAll()
                        .requestMatchers(HttpMethod.POST, CLASSIFY_MESSAGE_URL).permitAll()
                        .requestMatchers(HttpMethod.POST, EXTRACT_MEDICAL_INFO_URL).permitAll()

                        .requestMatchers(HttpMethod.GET, STATISTICS_ADMIN_URL).permitAll()

                        .requestMatchers(HttpMethod.POST, PROMPTS_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, PROMPTS_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, PROMPT_BY_ID_URL).permitAll()
                        .requestMatchers(HttpMethod.PATCH, ACTIVATE_PROMPT_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, CURRENT_PROMPT_URL).permitAll()

                        .anyRequest().authenticated()
                );

        RequestIdFilter requestIdFilter = requestIdFilterProvider.getIfAvailable();
        if (requestIdFilter != null) {
            http.addFilterBefore(requestIdFilter, UsernamePasswordAuthenticationFilter.class);
        }

        UserContextFilter userContextFilter = userContextFilterProvider.getIfAvailable();
        if (userContextFilter != null) {
            http.addFilterAfter(userContextFilter, RequestIdFilter.class);
        }

        AuthFilter authFilter = authFilterProvider.getIfAvailable();
        if (authFilter != null) {
            if (userContextFilter != null) {
                http.addFilterAfter(authFilter, UserContextFilter.class);
            } else {
                http.addFilterAfter(authFilter, RequestIdFilter.class);
            }
        }

        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("No local users configured");
        };
    }
}