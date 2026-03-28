package com.yazlab.dispatcher.filter;

import com.yazlab.dispatcher.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

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

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            sendError(response);
            return;
        }

        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        request.setAttribute("username", username);
        request.setAttribute("role", role);

        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if ("X-User".equals(name)) return username;
                if ("X-Role".equals(name)) return role;
                return super.getHeader(name);
            }
        };

        filterChain.doFilter(wrappedRequest, response);
    }

    private void sendError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"Yetkisiz erişim\"}");
        response.getWriter().flush();
    }
}