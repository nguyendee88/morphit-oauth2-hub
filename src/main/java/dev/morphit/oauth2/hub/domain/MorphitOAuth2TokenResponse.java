package dev.morphit.oauth2.hub.domain;

import java.util.List;

/**
 * @author morphit.dee88
 **/
public class MorphitOAuth2TokenResponse {
    
    private String clientId;
    private String projectId;
    
    private String provider; // ZaloOA, Facebook
    private String sourceId; // OaId, FanpageId, ..
    private String sourceName; // Morphit,..

    private String tokenType;
    private String refreshToken;
    private String accessToken;

    private List<String> scopes;

    private long expiredAt;
    private long createdAt;
    public String getProvider() {
        return provider;
    }
    public void setProvider(String provider) {
        this.provider = provider;
    }
    public String getSourceId() {
        return sourceId;
    }
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    public String getSourceName() {
        return sourceName;
    }
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    public String getTokenType() {
        return tokenType;
    }
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public List<String> getScopes() {
        return scopes;
    }
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }
    public long getExpiredAt() {
        return expiredAt;
    }
    public void setExpiredAt(long expiredAt) {
        this.expiredAt = expiredAt;
    }
    public long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public String getProjectId() {
        return projectId;
    }
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

}
