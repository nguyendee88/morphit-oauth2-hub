package dev.morphit.oauth2.hub.providers.googleads.domain;

import dev.morphit.oauth2.hub.domain.OAuth2RedeemTokenRequest;

/**
 * @author morphit.dee88
 **/
public class GoogleAdsOAuth2RedeemTokenRequest implements OAuth2RedeemTokenRequest {

    private String orgId;
    private String userId;

    private String authCode;

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getOrgId() {
        return orgId;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
