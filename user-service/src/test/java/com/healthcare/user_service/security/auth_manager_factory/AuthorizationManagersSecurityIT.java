package com.healthcare.user_service.security.auth_manager_factory;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.model.dto.auth.UserAuthInfoDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = AuthorizationManagersSecurityIT.TestSecurityConfig.class,
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.cloud.service-registry.auto-registration.enabled=false",
                "eureka.client.enabled=false",
                "auth-filter.enabled=false",
                "spring.liquibase.enabled=false",
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        }
)
@AutoConfigureMockMvc
@Import(AuthorizationManagersSecurityIT.TestSecurityConfig.class)
class AuthorizationManagersSecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void roleBased_shouldReturnForbidden_whenUserHasNoRequiredRole() throws Exception {
        mockMvc.perform(get("/test/users/id/10/role")
                        .with(authenticatedUser(1L, Set.of(Role.ROLE_PATIENT))))
                .andExpect(status().isForbidden());
    }

    @Test
    void roleBased_shouldReturnOk_whenUserHasRequiredRole() throws Exception {
        mockMvc.perform(get("/test/users/id/10/role")
                        .with(authenticatedUser(1L, Set.of(Role.ROLE_ADMIN))))
                .andExpect(status().isOk());
    }

    @Test
    void ownerBased_shouldReturnOk_whenUserIsOwner() throws Exception {
        mockMvc.perform(get("/test/users/id/10/owner")
                        .with(authenticatedUser(10L, Set.of(Role.ROLE_PATIENT))))
                .andExpect(status().isOk());
    }

    @Test
    void ownerBased_shouldReturnForbidden_whenUserIsNotOwner() throws Exception {
        mockMvc.perform(get("/test/users/id/10/owner")
                        .with(authenticatedUser(99L, Set.of(Role.ROLE_PATIENT))))
                .andExpect(status().isForbidden());
    }

    @Test
    void roleOrOwnerBased_shouldReturnOk_whenUserHasRequiredRole() throws Exception {
        mockMvc.perform(get("/test/users/id/10/role-or-owner")
                        .with(authenticatedUser(99L, Set.of(Role.ROLE_ADMIN))))
                .andExpect(status().isOk());
    }

    @Test
    void roleOrOwnerBased_shouldReturnOk_whenUserIsOwner() throws Exception {
        mockMvc.perform(get("/test/users/id/10/role-or-owner")
                        .with(authenticatedUser(10L, Set.of(Role.ROLE_PATIENT))))
                .andExpect(status().isOk());
    }

    @Test
    void roleOrOwnerBased_shouldReturnForbidden_whenUserIsNeitherOwnerNorHasRequiredRole() throws Exception {
        mockMvc.perform(get("/test/users/id/10/role-or-owner")
                        .with(authenticatedUser(99L, Set.of(Role.ROLE_PATIENT))))
                .andExpect(status().isForbidden());
    }

    @Test
    void securedEndpoints_shouldReturnUnauthorized_whenRequestIsUnauthenticated() throws Exception {
        mockMvc.perform(get("/test/users/id/10/role"))
                .andExpect(status().isUnauthorized());
    }

    private static RequestPostProcessor authenticatedUser(Long userId, Set<Role> roles) {
        UserAuthInfoDto principal = new UserAuthInfoDto(userId, roles);

        var auth = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                roles.stream()
                        .map(Role::name)
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );

        return authentication(auth);
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        AuthManagerFactory authManagerFactory() {
            return new AuthManagerFactoryImpl();
        }

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http,
                                                    AuthManagerFactory authManagerFactory) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .httpBasic(Customizer.withDefaults())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.GET, "/test/users/id/{id}/role")
                            .access(authManagerFactory.roleBased(Set.of(Role.ROLE_ADMIN)))

                            .requestMatchers(HttpMethod.GET, "/test/users/id/{id}/owner")
                            .access(authManagerFactory.ownerBased())

                            .requestMatchers(HttpMethod.GET, "/test/users/id/{id}/role-or-owner")
                            .access(authManagerFactory.roleOrOwnerBased(Set.of(Role.ROLE_ADMIN)))

                            .anyRequest().authenticated()
                    )
                    .build();
        }

        @RestController
        static class TestController {

            @GetMapping("/test/users/id/{id}/role")
            public String roleEndpoint(@PathVariable Long id) {
                return "ok";
            }

            @GetMapping("/test/users/id/{id}/owner")
            public String ownerEndpoint(@PathVariable Long id) {
                return "ok";
            }

            @GetMapping("/test/users/id/{id}/role-or-owner")
            public String roleOrOwnerEndpoint(@PathVariable Long id) {
                return "ok";
            }
        }
    }
}