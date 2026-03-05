package com.healthcare.user_service.filter;

import com.healthcare.user_service.filter.security.SignedUserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.healthcare.user_service.filter.security.constant.AttrNames.ATTR_REQUEST_ID;
import static com.healthcare.user_service.filter.security.constant.AttrNames.ATTR_USER_CONTEXT;

@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        var attrRid = request.getAttribute(ATTR_REQUEST_ID);

        if (!(attrRid instanceof String requestId) || !StringUtils.hasText(requestId)) {
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("Rid: " + requestId);

        var attrCtx = request.getAttribute(ATTR_USER_CONTEXT);
        if (!(attrCtx instanceof SignedUserContext ctx)) {
            filterChain.doFilter(request, response);
            return;
        }
        System.out.println("User id: " + ctx);

    }
}
