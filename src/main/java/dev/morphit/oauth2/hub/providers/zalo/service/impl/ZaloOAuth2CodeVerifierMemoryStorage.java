package dev.morphit.oauth2.hub.providers.zalo.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import dev.morphit.oauth2.hub.providers.zalo.service.ZaloOAuth2CodeVerifierStorage;

/**
 * @author morphit.dee88
 **/
@Service
public class ZaloOAuth2CodeVerifierMemoryStorage implements ZaloOAuth2CodeVerifierStorage {

    private Map<String, Map<String, String>> storage = new HashMap<String, Map<String, String>>();

    @Override
    public void put(String orgId, String clientId, String clientSecret, String codeVerifier) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("clientId", clientId);
        map.put("clientSecret", clientSecret);
        map.put("codeVerifier", codeVerifier);
        storage.put(orgId, map);
    }

    public Map<String, String> get(String orgId) {
        return storage.get(orgId);
    }

    @Override
    public void remove(String orgId) {
        storage.remove(orgId);
    }

}
