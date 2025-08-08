package dev.morphit.oauth2.hub.providers.tiktokads.domain;

import dev.morphit.oauth2.hub.domain.OAuth2ConsentRequest;

/**
* @author morphit.dee88
**/
public class TiktokAdsOAuth2ConsentRequest implements OAuth2ConsentRequest {
    
    private String orgId;
    private String userId;
    
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public String getRedirectUri() {
        return redirectUri;
    }
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
    public String getOrgId() {
        return orgId;
    }
    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getClientSecret() {
        return clientSecret;
    }
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
