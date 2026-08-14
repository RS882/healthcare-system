package com.healthcare.user_service.config;

import com.healthcare.user_service.config.configs_components.CustomAccessDeniedHandler;
import com.healthcare.user_service.config.configs_components.CustomAuthenticationEntryPoint;
import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.security.SecurityPublicEndpoints;
import com.healthcare.user_service.security.auth_manager_factory.AuthManagerFactory;
import com.healthcare.user_service.security.filter.AuthFilter;
import com.healthcare.user_service.security.filter.RequestIdFilter;
import com.healthcare.user_service.security.filter.UserContextFilter;
import com.healthcare.user_service.security.internal_request.constant.InternalAuthority;
import com.healthcare.user_service.security.internal_request.filter.InternalRequestAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Set;

import static com.healthcare.user_service.controller.API.ApiPaths.*;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@AllArgsConstructor
@ConditionalOnProperty(
        name = "security.config.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SecurityConfig {

    private final ObjectProvider<RequestIdFilter> requestIdFilterProvider;
    private final ObjectProvider<UserContextFilter> userContextFilterProvider;
    private final ObjectProvider<AuthFilter> authFilterProvider;
    private final AuthManagerFactory authManagerFactory;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    @Order(1)
    @ConditionalOnProperty(
            name = "internal-request-filter.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public SecurityFilterChain internalSecurityFilterChain(
            HttpSecurity http,
            InternalRequestAuthenticationFilter internalRequestAuthenticationFilter
    ) throws Exception {

        http
                .securityMatcher(INTERNAL_ALL_URL)

                .csrf(AbstractHttpConfigurer::disable)

                .cors(withDefaults())

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.POST,
                                INTERNAL_LOOKUP_URL
                        )
                        .hasAuthority(
                                InternalAuthority.USER_LOOKUP.authority()
                        )

                        .anyRequest().denyAll()
                )

                .addFilterBefore(
                        internalRequestAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                customAuthenticationEntryPoint
                        )
                        .accessDeniedHandler(
                                customAccessDeniedHandler
                        )
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain configureAuth(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(withDefaults())

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Public infrastructure endpoints
                        .requestMatchers(
                                SecurityPublicEndpoints.PUBLIC_ENDPOINTS
                        ).permitAll()

                        .requestMatchers(
                                EndpointRequest.to(HealthEndpoint.class)
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, REGISTRATION_URL).permitAll()

                        .requestMatchers(HttpMethod.GET, BY_ID_URL)
                        .access(authManagerFactory.roleOrOwnerBased(Set.of(Role.ROLE_ADMIN)))

                        .anyRequest().denyAll()

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
}
