package dev.morphit.oauth2.hub.providers.tiktokads.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author morphit.dee88
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TiktokOAuthAccessTokenApiResponse {

    @JsonProperty("code")
    private int code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("request_id")
    private String request_id;

    @JsonProperty("data")
    private TiktokOAuthAccessTokenApiDataObject data;

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

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public TiktokOAuthAccessTokenApiDataObject getData() {
        return data;
    }

    public void setData(TiktokOAuthAccessTokenApiDataObject data) {
        this.data = data;
    }
}
