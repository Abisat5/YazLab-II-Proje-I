package com.yazlab.dispatcher.service;

/**
 * Dispatcher uzerinden gelen istekler icin rol + HTTP metodu + yol bazli yetki.
 */
public interface AccessAuthorizationService {

    boolean canAccess(String role, String httpMethod, String requestPath);
}
