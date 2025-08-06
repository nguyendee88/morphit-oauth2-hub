package dev.morphit.oauth2.hub.session.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import dev.morphit.oauth2.hub.session.domain.MorphitOAuthSessionObject;
import dev.morphit.oauth2.hub.session.service.MorphitOAuthSessionService;

/**
 * @author morphit.dee88
 **/
@Service
public class MorphitOAuthSessionMemoryService implements MorphitOAuthSessionService {

    // Temporary in-memory storage.
    // For production, consider using a persistent store with TTL support (e.g.,
    // Redis or database with expiration).
    private Map<String, MorphitOAuthSessionObject> storage = new HashMap<String, MorphitOAuthSessionObject>();

    @Override
    public void put(String orgId, MorphitOAuthSessionObject object) {
        storage.put(orgId, object);
    }

    public MorphitOAuthSessionObject get(String orgId) {
        return storage.get(orgId);
    }

    @Override
    public void remove(String orgId) {
        storage.remove(orgId);
    }

}
