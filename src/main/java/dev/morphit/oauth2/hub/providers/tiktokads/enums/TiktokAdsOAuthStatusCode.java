package dev.morphit.oauth2.hub.providers.tiktokads.enums;

/**
 * @author morphit.dee88
 */
public enum TiktokAdsOAuthStatusCode {
    
    INVALID_AUTH_CODE(40110),
    INVALID_ACCESS_TOKEN(40105),
    SUCCESS(0);

    private int code;

    TiktokAdsOAuthStatusCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
