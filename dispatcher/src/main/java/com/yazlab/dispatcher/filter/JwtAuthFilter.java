package com.yazlab.dispatcher.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yazlab.dispatcher.exception.ErrorResponseBody;
import com.yazlab.dispatcher.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
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

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Yetkisiz erişim denemesi (Authorization yok veya Bearer değil)");
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Yetkisiz erişim");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            logger.warn("Geçersiz JWT token");
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Geçersiz token");
            return;
        }

        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        request.setAttribute("username", username);
        request.setAttribute("role", role);

        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if ("X-User".equals(name)) {
                    return username;
                }
                if ("X-Role".equals(name)) {
                    return role;
                }
                return super.getHeader(name);
            }
        };

        filterChain.doFilter(wrappedRequest, response);
        logger.debug("Authenticated user: {} | role: {}", username, role);
    }

    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = ErrorResponseBody.create(status, message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.getWriter().flush();
    }
}
