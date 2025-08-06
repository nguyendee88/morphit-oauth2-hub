package dev.morphit.oauth2.hub.domain;

/**
 * @author morphit.dee88
 **/
public interface OAuth2RedeemTokenRequest {

    String getUserId();

    String getOrgId();

    String getAuthCode();

}
