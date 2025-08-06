package dev.morphit.oauth2.hub.session.service;

import dev.morphit.oauth2.hub.session.domain.MorphitOAuthSessionObject;

/**
 * @author morphit.dee88
 **/
public interface MorphitOAuthSessionService {

    void put(String orgId, MorphitOAuthSessionObject object);

    MorphitOAuthSessionObject get(String orgId);
    
    void remove(String orgId);

}
