package dev.morphit.oauth2.hub.providers.tiktokads.service;

import java.net.URI;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.JsonObject;

import dev.morphit.oauth2.hub.domain.MorphitOAuth2TokenResponse;
import dev.morphit.oauth2.hub.domain.OAuth2ConsentRequest;
import dev.morphit.oauth2.hub.domain.OAuth2RedeemTokenRequest;
import dev.morphit.oauth2.hub.exception.MorphitOAuth2Exception;
import dev.morphit.oauth2.hub.providers.MorphitOAuthProvider;
import dev.morphit.oauth2.hub.providers.tiktokads.api.TiktokOAuthAccessTokenApiDataObject;
import dev.morphit.oauth2.hub.providers.tiktokads.api.TiktokOAuthAccessTokenApiResponse;
import dev.morphit.oauth2.hub.providers.tiktokads.api.TiktokOAuthMeApiDataObject;
import dev.morphit.oauth2.hub.providers.tiktokads.api.TiktokOAuthMeApiResponse;
import dev.morphit.oauth2.hub.providers.tiktokads.domain.TiktokAdsOAuth2ConsentRequest;
import dev.morphit.oauth2.hub.providers.tiktokads.domain.TiktokAdsOAuth2RedeemTokenRequest;
import dev.morphit.oauth2.hub.providers.tiktokads.enums.TiktokAdsOAuthStatusCode;
import dev.morphit.oauth2.hub.service.MorphitOAuth2TokenSupportService;
import dev.morphit.oauth2.hub.session.domain.MorphitOAuthSessionObject;
import dev.morphit.oauth2.hub.utils.PkceUtil;
import jakarta.annotation.PostConstruct;

/**
 * @author morphit.dee88
 * 
 * Document: https://business-api.tiktok.com/portal/docs
 * 
 **/
@Service("TiktokAdsOAuth2TokenService")
public class TiktokAdsOAuth2TokenService extends MorphitOAuth2TokenSupportService {

    private Logger logger = LoggerFactory.getLogger(TiktokAdsOAuth2TokenService.class);

    @Value("${morphit.oauth2.tiktokads.consent.endpoint:https://ads.tiktok.com/marketing_api/auth}")
    private String consentEndpoint;

    @Value("${morphit.oauth2.tiktokads.access_token.endpoint:https://business-api.tiktok.com/open_api/v1.3/oauth2/access_token/}")
    private String accessTokenEndpoint;

    @Value("${morphit.oauth2.tiktokads.user_info.endpoint:https://business-api.tiktok.com/open_api/v1.3/user/info/}")
    private String userInfoEndpoint;

    @PostConstruct
    public void init() {
        serviceHandler.register(provider().name(), this);
    }

    @Override
    public MorphitOAuthProvider provider() {
        return MorphitOAuthProvider.TiktokAdsOAuth;
    }

    @Override
    public String buildConsentEndpoint(OAuth2ConsentRequest request) {
        TiktokAdsOAuth2ConsentRequest tiktokAdsRequest = (TiktokAdsOAuth2ConsentRequest) request;
        String clientId = tiktokAdsRequest.getClientId();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(URI.create(consentEndpoint));
        builder.queryParam("app_id", clientId);
        builder.queryParam("redirect_uri", tiktokAdsRequest.getRedirectUri());
        String sessionId = PkceUtil.generateCodeVerifier();

        MorphitOAuthSessionObject sessionObject = new MorphitOAuthSessionObject();
        sessionObject.setOrgId(request.getOrgId());
        sessionObject.setUserId(request.getUserId());
        sessionObject.setClientId(tiktokAdsRequest.getClientId());
        sessionObject.setClientSecret(tiktokAdsRequest.getClientSecret());
        sessionObject.setRedirectUri(tiktokAdsRequest.getRedirectUri());
        sessionObject.setSessionId(sessionId);

        return builder.build().toString();
    }

    @Override
    public MorphitOAuth2TokenResponse redeemToken(OAuth2RedeemTokenRequest request) throws MorphitOAuth2Exception {
        TiktokAdsOAuth2RedeemTokenRequest tiktokAdsRequest = (TiktokAdsOAuth2RedeemTokenRequest) request;
        String orgId = request.getOrgId();

        MorphitOAuthSessionObject sessionObject = morphitOAuthSessionService.get(request.getOrgId());
        if (sessionObject == null) {
            String msg = String.format("Code Verifier not found for orgId: %s", request.getOrgId());
            throw new MorphitOAuth2Exception(msg);
        }

        String clientId = sessionObject.getClientId();
        String clientSecret = sessionObject.getClientSecret();
        String redirectUri = sessionObject.getRedirectUri();
        if (StringUtils.isAnyBlank(clientId, clientSecret, redirectUri)) {
            String msg = String.format("Missing app credentials for provider [%s], orgId: %s", provider(), orgId);
            throw new MorphitOAuth2Exception(msg);
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(URI.create(accessTokenEndpoint));
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("auth_code", tiktokAdsRequest.getAuthCode());
        jsonObject.addProperty("app_id", clientId);
        jsonObject.addProperty("secret", clientSecret);

        URI uri = builder.build().encode().toUri();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> httpEntity = new HttpEntity<>(jsonObject.toString(), headers);
            HttpEntity<TiktokOAuthAccessTokenApiResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.POST,
                    httpEntity, TiktokOAuthAccessTokenApiResponse.class);

            TiktokOAuthAccessTokenApiResponse responseBody = responseEntity.getBody();
            if (responseBody == null) {
                throw new MorphitOAuth2Exception("Empty response from TiktokAds token endpoint");
            }
            TiktokOAuthAccessTokenApiDataObject dataObject = responseBody.getData();
            String accessToken = dataObject.getAccess_token();
            if (StringUtils.isBlank(accessToken)) {
                throw new MorphitOAuth2Exception("Error during authenticated with Tiktok - access token is null");
            }
            TiktokOAuthMeApiDataObject userInfo = fetchUserInfo(accessToken);

            MorphitOAuth2TokenResponse tokenResponse = new MorphitOAuth2TokenResponse();
            tokenResponse.setClientId(clientId);
            tokenResponse.setProvider(MorphitOAuthProvider.TiktokAdsOAuth.name());
            if (userInfo != null) {
                tokenResponse.setSourceId(userInfo.getId());
                tokenResponse.setSourceName(userInfo.getEmail());
            }
            tokenResponse.setTokenType("access_token");
            tokenResponse.setAccessToken(accessToken);
            
            tokenResponse.setScopes(new ArrayList<String>());
            if(dataObject.getScopes() != null) {
                for(int scope : dataObject.getScopes()) {
                    tokenResponse.getScopes().add(String.valueOf(scope));
                }
            }

            long now = System.currentTimeMillis();
            tokenResponse.setCreatedAt(now);

            return tokenResponse;
        } catch (HttpClientErrorException e) {
            String statusText = e.getStatusText();
            String responseBody = e.getResponseBodyAsString();
            logger.error("TiktokAds Auth Error - URI: {} - Status: {} ({}) - Body: {}", uri, e.getStatusCode().value(),
                    statusText, responseBody);
            String msg = String.format("TiktokAds authentication failed (HTTP %d): %s", e.getStatusCode().value(),
                    StringUtils.defaultIfBlank(responseBody, statusText));
            throw new MorphitOAuth2Exception(msg);
        } catch (Exception e) {
            logger.error("Unexpected error during TiktokAds authentication - URI: {}", uri, e);
            String msg = String.format("Unexpected error during TiktokAds authentication: %s",
                    StringUtils.defaultIfBlank(e.getMessage(), "No details"));

            throw new MorphitOAuth2Exception(msg);
        } finally {
            morphitOAuthSessionService.remove(orgId);
        }
    }

    private TiktokOAuthMeApiDataObject fetchUserInfo(String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(URI.create(userInfoEndpoint));
        URI uri = builder.build().encode().toUri();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Access-Token", accessToken);
            HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);
            HttpEntity<TiktokOAuthMeApiResponse> entityResponse = restTemplate.exchange(uri, HttpMethod.GET, httpEntity,
                    TiktokOAuthMeApiResponse.class);

            TiktokOAuthMeApiResponse body = entityResponse.getBody();
            if (body == null) {
                logger.error("Empty TikTok user info response");
                return null;
            }

            if (body.getCode() != TiktokAdsOAuthStatusCode.SUCCESS.getCode()) {
                logger.error("TikTok user info error. code={}, message={}", body.getCode(), body.getMessage());
                return null;
            }

            return body.getData();
        } catch (HttpClientErrorException e) {
            logger.error("TikTok user info HTTP {} - {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected TikTok user info error", e);
            return null;
        }
    }

}
