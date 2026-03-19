package com.yazlab.dispatcher;

import com.yazlab.dispatcher.filter.JwtAuthFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DispatcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(DispatcherApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilter(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/users/*", "/messages/*"); 
        return registrationBean;
    }
}