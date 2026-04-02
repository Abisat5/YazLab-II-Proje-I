package com.yazlab.dispatcher.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "access_rules")
public class AccessRule {

    @Id
    private String id;
    @NotBlank
    private String role;
    /** HTTP metodu veya tumu icin "*" */
    @NotBlank
    private String httpMethod;
    /** Ant tarzi yol deseni (orn: /conversations/**) */
    @NotBlank
    private String pathPattern;

    public AccessRule() {
    }

    public AccessRule(String id, String role, String httpMethod, String pathPattern) {
        this.id = id;
        this.role = role;
        this.httpMethod = httpMethod;
        this.pathPattern = pathPattern;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }
}
