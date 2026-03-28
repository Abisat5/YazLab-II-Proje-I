package com.yazlab.dispatcher.config;

import com.yazlab.dispatcher.filter.AccessAuthorizationFilter;
import com.yazlab.dispatcher.filter.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AccessAuthorizationFilter accessAuthorizationFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          AccessAuthorizationFilter accessAuthorizationFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.accessAuthorizationFilter = accessAuthorizationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF kapalı
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // stateless
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // tüm requestlere izin
            );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(accessAuthorizationFilter, JwtAuthFilter.class);

        return http.build();
    }
}