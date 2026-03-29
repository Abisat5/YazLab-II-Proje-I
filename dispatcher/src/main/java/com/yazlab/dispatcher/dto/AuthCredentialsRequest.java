package com.yazlab.dispatcher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Auth-service login/register ile ayni JSON govdesi: {"username":"...","password":"..."}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthCredentialsRequest {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
