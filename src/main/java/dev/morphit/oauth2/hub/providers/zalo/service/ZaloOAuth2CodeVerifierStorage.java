package dev.morphit.oauth2.hub.providers.zalo.service;

import java.util.Map;

/**
 * @author morphit.dee88
 **/
public interface ZaloOAuth2CodeVerifierStorage {

    Map<String, String> get(String orgId);

    void put(String orgId, String clientId, String clientSecret, String codeVerifier);
    
    void remove(String orgId);
}
