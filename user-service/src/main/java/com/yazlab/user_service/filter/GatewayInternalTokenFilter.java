package com.yazlab.user_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(0)
public class GatewayInternalTokenFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Yazlab-Internal-Token";

    private final String expectedToken;

    public GatewayInternalTokenFilter(@Value("${yazlab.internal-gateway-token}") String expectedToken) {
        this.expectedToken = expectedToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(HEADER);
        if (token == null || !token.equals(expectedToken)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"error\":\"Dogrudan erisim reddedildi; yalnizca API Gateway uzerinden erisin\"}");
            response.getWriter().flush();
            return;
        }
        filterChain.doFilter(request, response);
    }
}
