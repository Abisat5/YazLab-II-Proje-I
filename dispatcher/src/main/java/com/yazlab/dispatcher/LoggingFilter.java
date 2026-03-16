package com.yazlab.dispatcher;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// @Component("loggingFilter") sayesinde az önce patlayan testimiz artık bu sınıfı bulabilecek!
@Component("loggingFilter")
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // İsteği asıl servislere geçir
        filterChain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime;

        // Trafik bilgilerini logla
        logger.info("METHOD: {}, URI: {}, STATUS: {}, DURATION: {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration);
    }
}