package com.healthcare.user_service.config;

import com.healthcare.user_service.filter.AuthFilter;
import com.healthcare.user_service.filter.RequestIdFilter;
import com.healthcare.user_service.filter.UserContextFilter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.healthcare.user_service.controller.API.ApiPaths.*;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final RequestIdFilter requestIdFilter;
    private final ObjectProvider<UserContextFilter> userContextFilterProvider;
    private final ObjectProvider<AuthFilter> authFilterProvider;

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
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, REGISTRATION_URL).permitAll()
                        .requestMatchers(HttpMethod.POST, LOOKUP_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, BY_ID_URL).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(requestIdFilter, UsernamePasswordAuthenticationFilter.class);

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
        return http.build();
    }
}
