package dev.morphit.oauth2.hub.providers.facebook.service;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import dev.morphit.oauth2.hub.domain.MorphitOAuth2TokenResponse;
import dev.morphit.oauth2.hub.domain.OAuth2ConsentRequest;
import dev.morphit.oauth2.hub.domain.OAuth2RedeemTokenRequest;
import dev.morphit.oauth2.hub.exception.MorphitOAuth2Exception;
import dev.morphit.oauth2.hub.providers.MorphitOAuthProvider;
import dev.morphit.oauth2.hub.providers.facebook.api.FacebookOAuthMeApiResponse;
import dev.morphit.oauth2.hub.providers.facebook.api.FacebookOAuthTokenApiResponse;
import dev.morphit.oauth2.hub.providers.facebook.domain.FacebookOAuth2ConsentRequest;
import dev.morphit.oauth2.hub.providers.facebook.domain.FacebookOAuth2RedeemTokenRequest;
import dev.morphit.oauth2.hub.service.MorphitOAuth2TokenSupportService;
import dev.morphit.oauth2.hub.session.domain.MorphitOAuthSessionObject;
import dev.morphit.oauth2.hub.utils.PkceUtil;
import jakarta.annotation.PostConstruct;

/**
 * @author morphit.dee88
 **/
@Service("FacebookOAuth2TokenService")
public class FacebookOAuth2TokenService extends MorphitOAuth2TokenSupportService {

    private Logger logger = LoggerFactory.getLogger(FacebookOAuth2TokenService.class);

    @Value("${morphit.oauth2.facebook.consent.endpoint:https://www.facebook.com/dialog/oauth}")
    private String consentEndpoint;

    @Value("${morphit.oauth2.facebook.consent.scopes}")
    private String consentScopes;

    @Value("${morphit.oauth2.facebook.access_token.endpoint:https://graph.facebook.com/oauth/access_token}")
    private String accessTokenEndpoint;

    private static final String USER_INFO_API = "https://graph.facebook.com/me";

    @PostConstruct
    public void init() {
        serviceHandler.register(provider().name(), this);
    }
    
    @Override
    public MorphitOAuthProvider provider() {
        return MorphitOAuthProvider.FacebookOAuth;
    }

    @Override
    public String buildConsentEndpoint(OAuth2ConsentRequest request) {
        FacebookOAuth2ConsentRequest facebookRequest = (FacebookOAuth2ConsentRequest) request;
        String clientId = facebookRequest.getClientId();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(URI.create(consentEndpoint));
        builder.queryParam("scope", consentScopes);
        builder.queryParam("client_id", clientId);
        builder.queryParam("redirect_uri", facebookRequest.getRedirectUri());
        String sessionId = PkceUtil.generateCodeVerifier();

        MorphitOAuthSessionObject sessionObject = new MorphitOAuthSessionObject();
        sessionObject.setOrgId(request.getOrgId());
        sessionObject.setUserId(request.getUserId());
        sessionObject.setClientId(facebookRequest.getClientId());
        sessionObject.setClientSecret(facebookRequest.getClientSecret());
        sessionObject.setRedirectUri(facebookRequest.getRedirectUri());
        sessionObject.setSessionId(sessionId);

        return builder.build().toString();
    }

    @Override
    public MorphitOAuth2TokenResponse redeemToken(OAuth2RedeemTokenRequest request) throws MorphitOAuth2Exception {
        FacebookOAuth2RedeemTokenRequest facebookRequest = (FacebookOAuth2RedeemTokenRequest) request;
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
        builder.queryParam("client_id", clientId);
        builder.queryParam("client_secret", clientSecret);
        builder.queryParam("redirect_uri", redirectUri);
        builder.queryParam("code", facebookRequest.getAuthCode());

        URI uri = builder.build().encode().toUri();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<FacebookOAuthTokenApiResponse> responseEntity = restTemplate.getForEntity(uri,
                    FacebookOAuthTokenApiResponse.class);
            FacebookOAuthTokenApiResponse responseBody = responseEntity.getBody();
            if (responseBody == null) {
                throw new MorphitOAuth2Exception("Empty response from Facebook token endpoint");
            }
            String accessToken = responseBody.getAccessToken();
            FacebookOAuthMeApiResponse userInfo = fetchUserInfo(accessToken);

            MorphitOAuth2TokenResponse tokenResponse = new MorphitOAuth2TokenResponse();
            tokenResponse.setClientId(clientId);
            tokenResponse.setProvider(MorphitOAuthProvider.FacebookOAuth.name());
            if (userInfo != null) {
                tokenResponse.setSourceId(userInfo.getId());
                tokenResponse.setSourceName(userInfo.getName());
            }
            tokenResponse.setTokenType(responseBody.getTokenType());
            tokenResponse.setAccessToken(responseBody.getAccessToken());

            long now = System.currentTimeMillis();
            tokenResponse.setCreatedAt(now);
            tokenResponse.setExpiredAt(now + responseBody.getExpiredIn() * 1000L);

            return tokenResponse;
        } catch (HttpClientErrorException e) {
            String statusText = e.getStatusText();
            String responseBody = e.getResponseBodyAsString();
            logger.error("Facebook Auth Error - URI: {} - Status: {} ({}) - Body: {}", uri, e.getStatusCode().value(),
                    statusText, responseBody);
            String msg = String.format("Facebook authentication failed (HTTP %d): %s", e.getStatusCode().value(),
                    StringUtils.defaultIfBlank(responseBody, statusText));
            throw new MorphitOAuth2Exception(msg);
        } catch (Exception e) {
            logger.error("Unexpected error during Facebook authentication - URI: {}", uri, e);
            String msg = String.format("Unexpected error during Facebook authentication: %s",
                    StringUtils.defaultIfBlank(e.getMessage(), "No details"));

            throw new MorphitOAuth2Exception(msg);
        } finally {
            morphitOAuthSessionService.remove(orgId);
        }
    }

    private FacebookOAuthMeApiResponse fetchUserInfo(String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(URI.create(USER_INFO_API));
        builder.queryParam("access_token", accessToken);
        URI uri = builder.build().encode().toUri();
        try {
            HttpEntity<FacebookOAuthMeApiResponse> entityResponse = restTemplate.getForEntity(uri,
                    FacebookOAuthMeApiResponse.class);
            FacebookOAuthMeApiResponse response = entityResponse.getBody();
            if (response == null) {
                logger.error("Empty response while authenticating with Facebook - URI: {}", uri);
            }
            return response;
        } catch (HttpClientErrorException e) {
            logger.error("Facebook Auth Error - URI: {} - Status: {} ({}) - Body: {}", uri, e.getStatusCode().value(),
                    e.getStatusText(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error during Facebook authentication - URI: {}", uri, e);
            return null;
        }
    }
}
