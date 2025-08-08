package dev.morphit.oauth2.hub.providers.tiktokads.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* @author morphit.dee88
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TiktokOAuthAccessTokenApiDataObject {
    
    @JsonProperty("access_token")
    private String access_token;

    @JsonProperty("advertiser_ids")
    private List<String> advertiser_ids;

    @JsonProperty("scopes")
    private List<Integer> scopes;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public List<String> getAdvertiser_ids() {
        return advertiser_ids;
    }

    public void setAdvertiser_ids(List<String> advertiser_ids) {
        this.advertiser_ids = advertiser_ids;
    }

    public List<Integer> getScopes() {
        return scopes;
    }

    public void setScopes(List<Integer> scopes) {
        this.scopes = scopes;
    }
}
