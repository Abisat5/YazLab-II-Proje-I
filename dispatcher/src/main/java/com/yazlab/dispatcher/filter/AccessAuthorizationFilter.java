package com.yazlab.dispatcher.filter;

import com.yazlab.dispatcher.service.AccessAuthorizationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AccessAuthorizationFilter extends OncePerRequestFilter {

    private final AccessAuthorizationService accessAuthorizationService;

    public AccessAuthorizationFilter(AccessAuthorizationService accessAuthorizationService) {
        this.accessAuthorizationService = accessAuthorizationService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/auth/")
                || uri.startsWith("/actuator/")
                || uri.equals("/ready");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Object roleObj = request.getAttribute("role");
        if (roleObj == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String role = roleObj.toString();
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (!accessAuthorizationService.canAccess(role, method, path)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Bu kaynak icin yetkiniz yok\"}");
            response.getWriter().flush();
            return;
        }
        filterChain.doFilter(request, response);
    }
}
