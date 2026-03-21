package com.yazlab.dispatcher.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String VALID_TOKEN = "mock-token"; // doğru token

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response);
            return;
        }

        String token = authHeader.substring(7); // "Bearer " kısmını at
        if (!VALID_TOKEN.equals(token)) {
            sendErrorResponse(response);
            return;
        }

        // Token doğruysa devam et
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        // 200 OK yerine 401 Unauthorized dönüyoruz
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"Yetkisiz erişim: Geçersiz veya eksik token\"}");
        response.getWriter().flush();
    }
}