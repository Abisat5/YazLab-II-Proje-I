package com.yazlab.dispatcher.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yazlab.services")
public class ServiceUrlProperties {

    /**
     * user-service taban URL (ornek: http://user-service:8082)
     */
    private String user = "http://user-service:8082";
    private String message = "http://message-service:8083";
    private String auth = "http://auth-service:8081";

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
}
