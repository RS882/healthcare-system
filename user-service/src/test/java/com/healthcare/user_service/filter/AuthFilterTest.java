package com.healthcare.user_service.filter;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.filter.security.SignedUserContext;
import com.healthcare.user_service.model.dto.auth.UserAuthInfoDto;
import com.healthcare.user_service.service.interfacies.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.healthcare.user_service.filter.security.constant.AttrNames.ATTR_REQUEST_ID;
import static com.healthcare.user_service.filter.security.constant.AttrNames.ATTR_USER_CONTEXT;
import static com.healthcare.user_service.support.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Auth filter tests: ")
class AuthFilterTest {

    @Mock
    private UserService userService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_continue_filter_chain_when_authentication_already_exists()
            throws ServletException, IOException {

        Authentication existingAuth =
                new UsernamePasswordAuthenticationToken("existing-user", null, List.of());

        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        filter.doFilterInternal(request, response, filterChain);

        assertSame(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_request_id_attribute_is_missing()
            throws ServletException, IOException {

        SignedUserContext ctx = mock(SignedUserContext.class);
        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userService);
        verifyNoInteractions(ctx);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_request_id_attribute_is_not_string()
            throws ServletException, IOException {

        request.setAttribute(ATTR_REQUEST_ID, 123L);

        SignedUserContext ctx = mock(SignedUserContext.class);
        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userService);
        verifyNoInteractions(ctx);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_request_id_attribute_is_blank()
            throws ServletException, IOException {

        request.setAttribute(ATTR_REQUEST_ID, "   ");

        SignedUserContext ctx = mock(SignedUserContext.class);
        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userService);
        verifyNoInteractions(ctx);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_user_context_attribute_is_missing()
            throws ServletException, IOException {

        request.setAttribute(ATTR_REQUEST_ID, requestId().toString());

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_user_context_attribute_has_wrong_type()
            throws ServletException, IOException {

        request.setAttribute(ATTR_REQUEST_ID, requestId().toString());
        request.setAttribute(ATTR_USER_CONTEXT, "not-a-context");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_context_request_id_is_blank()
            throws ServletException, IOException {

        request.setAttribute(ATTR_REQUEST_ID, requestId().toString());

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn("   ");

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(ctx).requestId();
        verifyNoInteractions(userService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_request_id_does_not_match_context_request_id()
            throws ServletException, IOException {
        String rid1 = requestId().toString();
        String rid2 = requestId().toString();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid2);

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(ctx).requestId();
        verifyNoInteractions(userService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_user_id_is_blank()
            throws ServletException, IOException {
        String rid1 = requestId().toString();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid1);
        when(ctx.userId()).thenReturn("   ");

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(ctx).requestId();
        verify(ctx).userId();
        verifyNoInteractions(userService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_user_id_is_not_a_number()
            throws ServletException, IOException {

        String rid1 = requestId().toString();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid1);
        when(ctx.userId()).thenReturn("abc");

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(ctx).requestId();
        verify(ctx).userId();
        verifyNoInteractions(userService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_user_id_is_not_positive()
            throws ServletException, IOException {
        String rid1 = requestId().toString();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid1);
        when(ctx.userId()).thenReturn("0");

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(ctx).requestId();
        verify(ctx).userId();
        verifyNoInteractions(userService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_user_service_returns_null()
            throws ServletException, IOException {

        String rid1 = requestId().toString();
        Long userId = randomUserId();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid1);
        when(ctx.userId()).thenReturn(userId.toString());

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        when(userService.getUserAuthInfoDtoById(userId)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(ctx).requestId();
        verify(ctx).userId();
        verify(userService).getUserAuthInfoDtoById(userId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_auth_dto_user_id_is_null()
            throws ServletException, IOException {

        String rid1 = requestId().toString();
        Long userId = randomUserId();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid1);
        when(ctx.userId()).thenReturn(userId.toString());

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        UserAuthInfoDto authDto = new UserAuthInfoDto(null, Set.of(Role.ROLE_PATIENT));
        when(userService.getUserAuthInfoDtoById(userId)).thenReturn(authDto);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(ctx).requestId();
        verify(ctx).userId();
        verify(userService).getUserAuthInfoDtoById(userId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_context_user_id_does_not_match_auth_dto_user_id()
            throws ServletException, IOException {

        String rid1 = requestId().toString();
        Long userId1 = randomUserId();
        Long userId2 = randomUserId();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid1);
        when(ctx.userId()).thenReturn(userId1.toString());

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        UserAuthInfoDto authDto = new UserAuthInfoDto(userId2, Set.of(Role.ROLE_PATIENT));
        when(userService.getUserAuthInfoDtoById(userId1)).thenReturn(authDto);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(ctx).requestId();
        verify(ctx, times(2)).userId();
        verify(userService).getUserAuthInfoDtoById(userId1);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_actual_roles_are_null()
            throws ServletException, IOException {

        String rid1 = requestId().toString();
        Long userId = randomUserId();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid1);
        when(ctx.userId()).thenReturn(userId.toString());

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        UserAuthInfoDto authDto = new UserAuthInfoDto(userId, null);
        when(userService.getUserAuthInfoDtoById(userId)).thenReturn(authDto);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(ctx).requestId();
        verify(ctx, times(2)).userId();
        verify(ctx).roles();
        verify(userService).getUserAuthInfoDtoById(userId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_actual_roles_are_empty()
            throws ServletException, IOException {

        String rid1 = requestId().toString();
        Long userId = randomUserId();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid1);
        when(ctx.userId()).thenReturn(userId.toString());

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        UserAuthInfoDto authDto = new UserAuthInfoDto(userId, Set.of());
        when(userService.getUserAuthInfoDtoById(userId)).thenReturn(authDto);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(ctx).requestId();
        verify(ctx, times(2)).userId();
        verify(ctx).roles();
        verify(userService).getUserAuthInfoDtoById(userId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_token_roles_are_null()
            throws ServletException, IOException {

        String rid1 = requestId().toString();
        Long userId = randomUserId();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid1);
        when(ctx.userId()).thenReturn(userId.toString());
        when(ctx.roles()).thenReturn(null);

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        UserAuthInfoDto authDto = new UserAuthInfoDto(userId, Set.of(Role.ROLE_PATIENT));
        when(userService.getUserAuthInfoDtoById(userId)).thenReturn(authDto);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(ctx).requestId();
        verify(ctx, times(2)).userId();
        verify(ctx).roles();
        verify(userService).getUserAuthInfoDtoById(userId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_token_roles_are_empty()
            throws ServletException, IOException {

        String rid1 = requestId().toString();
        Long userId = randomUserId();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid1);
        when(ctx.userId()).thenReturn(userId.toString());
        when(ctx.roles()).thenReturn(List.of());

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        UserAuthInfoDto authDto = new UserAuthInfoDto(userId, Set.of(Role.ROLE_PATIENT));
        when(userService.getUserAuthInfoDtoById(userId)).thenReturn(authDto);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(ctx).requestId();
        verify(ctx, times(2)).userId();
        verify(ctx).roles();
        verify(userService).getUserAuthInfoDtoById(userId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_continue_when_roles_do_not_match()
            throws ServletException, IOException {

        String rid1 = requestId().toString();
        Long userId = randomUserId();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid1);
        when(ctx.userId()).thenReturn(userId.toString());
        when(ctx.roles()).thenReturn(List.of(Role.ROLE_PATIENT.name()));

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        UserAuthInfoDto authDto = new UserAuthInfoDto(userId, Set.of(Role.ROLE_ADMIN));
        when(userService.getUserAuthInfoDtoById(userId)).thenReturn(authDto);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(ctx).requestId();
        verify(ctx, times(2)).userId();
        verify(ctx).roles();
        verify(userService).getUserAuthInfoDtoById(userId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_set_authentication_when_context_is_valid()
            throws ServletException, IOException {

        String rid1 = requestId().toString();
        Long userId = randomUserId();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid1);
        when(ctx.userId()).thenReturn(userId.toString());
        when(ctx.roles()).thenReturn(List.of(Role.ROLE_PATIENT.name(), Role.ROLE_ADMIN.name()));

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        UserAuthInfoDto authDto = new UserAuthInfoDto(
                userId,
                Set.of(Role.ROLE_PATIENT, Role.ROLE_ADMIN)
        );
        when(userService.getUserAuthInfoDtoById(userId)).thenReturn(authDto);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertInstanceOf(UsernamePasswordAuthenticationToken.class, authentication);
        assertSame(authDto, authentication.getPrincipal());
        assertNull(authentication.getCredentials());

        Set<String> authorities = authentication.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());

        assertEquals(Set.of(Role.ROLE_PATIENT.name(), Role.ROLE_ADMIN.name()), authorities);

        assertNotNull(authentication.getDetails());

        verify(ctx).requestId();
        verify(ctx, times(2)).userId();
        verify(ctx).roles();
        verify(userService).getUserAuthInfoDtoById(userId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_trim_request_id_user_id_and_roles_before_comparison()
            throws ServletException, IOException {

        String rid1 = requestId().toString();
        Long userId = randomUserId();
        request.setAttribute(ATTR_REQUEST_ID, rid1);

        SignedUserContext ctx = mock(SignedUserContext.class);
        when(ctx.requestId()).thenReturn(rid1);
        when(ctx.userId()).thenReturn(stringWithSpaces(userId.toString()));
        when(ctx.roles()).thenReturn(List.of(
                stringWithSpaces(Role.ROLE_PATIENT.name()),
                stringWithSpaces(Role.ROLE_ADMIN.name())));

        request.setAttribute(ATTR_USER_CONTEXT, ctx);

        UserAuthInfoDto authDto = new UserAuthInfoDto(
                userId,
                Set.of(Role.ROLE_PATIENT, Role.ROLE_ADMIN)
        );
        when(userService.getUserAuthInfoDtoById(userId)).thenReturn(authDto);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertSame(authDto, authentication.getPrincipal());

        Set<String> authorities = authentication.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());

        assertEquals(Set.of(Role.ROLE_PATIENT.name(), Role.ROLE_ADMIN.name()), authorities);

        verify(ctx).requestId();
        verify(ctx, times(2)).userId();
        verify(ctx).roles();
        verify(userService).getUserAuthInfoDtoById(userId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_not_filter_for_actuator_path() {
        request.setServletPath("/actuator/health");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void should_not_filter_for_swagger_ui_path() {
        request.setServletPath("/swagger-ui/index.html");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void should_not_filter_for_api_docs_path() {
        request.setServletPath("/v3/api-docs");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void should_filter_for_regular_path() {
        request.setServletPath("/api/users/me");
        assertFalse(filter.shouldNotFilter(request));
    }

}