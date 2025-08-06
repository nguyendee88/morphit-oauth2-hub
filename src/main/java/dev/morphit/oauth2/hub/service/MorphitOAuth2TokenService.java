package dev.morphit.oauth2.hub.service;

import dev.morphit.oauth2.hub.domain.MorphitOAuth2TokenResponse;
import dev.morphit.oauth2.hub.domain.OAuth2ConsentRequest;
import dev.morphit.oauth2.hub.domain.OAuth2RedeemTokenRequest;
import dev.morphit.oauth2.hub.exception.MorphitOAuth2Exception;
import dev.morphit.oauth2.hub.providers.MorphitOAuthProvider;

/**
* @author morphit.dee88
**/
public interface MorphitOAuth2TokenService {
    
    MorphitOAuthProvider provider();
    
    String buildConsentEndpoint(OAuth2ConsentRequest request);
    
    MorphitOAuth2TokenResponse redeemToken(OAuth2RedeemTokenRequest request) throws MorphitOAuth2Exception;
}
