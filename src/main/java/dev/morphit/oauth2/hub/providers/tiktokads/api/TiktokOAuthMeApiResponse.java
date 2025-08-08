package dev.morphit.oauth2.hub.providers.tiktokads.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author morphit.dee88
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TiktokOAuthMeApiResponse {

    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("data")
    private TiktokOAuthMeApiDataObject data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TiktokOAuthMeApiDataObject getData() {
        return data;
    }

    public void setData(TiktokOAuthMeApiDataObject data) {
        this.data = data;
    }
}
