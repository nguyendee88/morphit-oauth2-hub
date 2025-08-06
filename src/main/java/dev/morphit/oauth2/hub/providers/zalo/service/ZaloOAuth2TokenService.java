package dev.morphit.oauth2.hub.providers.zalo.service;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import dev.morphit.oauth2.hub.domain.MorphitOAuth2TokenResponse;
import dev.morphit.oauth2.hub.domain.OAuth2ConsentRequest;
import dev.morphit.oauth2.hub.domain.OAuth2RedeemTokenRequest;
import dev.morphit.oauth2.hub.exception.MorphitOAuth2Exception;
import dev.morphit.oauth2.hub.providers.MorphitOAuthProvider;
import dev.morphit.oauth2.hub.providers.zalo.api.ZaloOAErrorApiResponse;
import dev.morphit.oauth2.hub.providers.zalo.api.ZaloOATokenApiResponse;
import dev.morphit.oauth2.hub.providers.zalo.domain.ZaloOAuth2ConsentRequest;
import dev.morphit.oauth2.hub.providers.zalo.domain.ZaloOAuth2RedeemTokenRequest;
import dev.morphit.oauth2.hub.service.MorphitOAuth2TokenSupportService;
import dev.morphit.oauth2.hub.session.domain.MorphitOAuthSessionObject;
import dev.morphit.oauth2.hub.utils.PkceUtil;
import jakarta.annotation.PostConstruct;

/**
 * @author morphit.dee88
 **/
@Service("ZaloOAuth2TokenService")
public class ZaloOAuth2TokenService extends MorphitOAuth2TokenSupportService {

    private Logger logger = LoggerFactory.getLogger(ZaloOAuth2TokenService.class);

    @Value("${morphit.oauth2.zalo.consent.endpoint:https://oauth.zalo.me/v4/permission}")
    private String permissionEndpoint;

    @Value("${morphit.oauth2.zalo.access_token.endpoint:https://oauth.zaloapp.com/v4/oa/access_token}")
    private String accessTokenEndpoint;

    @PostConstruct
    public void init() {
        serviceHandler.register(provider().name(), this);
    }
    
    @Override
    public MorphitOAuthProvider provider() {
        return MorphitOAuthProvider.ZaloOAuth;
    }

    @Override
    public String buildConsentEndpoint(OAuth2ConsentRequest request) {
        ZaloOAuth2ConsentRequest zaloRequest = (ZaloOAuth2ConsentRequest) request;
        String clientId = zaloRequest.getClientId();

        String codeVerifier = PkceUtil.generateCodeVerifier();
        String codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(URI.create(permissionEndpoint));
        builder.queryParam("app_id", clientId);
        builder.queryParam("redirect_uri", zaloRequest.getRedirectUri());
        builder.queryParam("code_challenge", codeChallenge);

        MorphitOAuthSessionObject sessionObject = new MorphitOAuthSessionObject();
        sessionObject.setOrgId(request.getOrgId());
        sessionObject.setUserId(request.getUserId());
        sessionObject.setClientId(zaloRequest.getClientId());
        sessionObject.setClientSecret(zaloRequest.getClientSecret());
        sessionObject.setSessionId(codeVerifier);

        morphitOAuthSessionService.put(zaloRequest.getOrgId(), sessionObject);
        return builder.build().toString();
    }

    @Override
    public MorphitOAuth2TokenResponse redeemToken(OAuth2RedeemTokenRequest request) throws MorphitOAuth2Exception {
        ZaloOAuth2RedeemTokenRequest zaloRequest = (ZaloOAuth2RedeemTokenRequest) request;
        String orgId = request.getOrgId();

        // Retrieve stored PKCE code_verifier and client info (from previous consent
        // step)
        MorphitOAuthSessionObject sessionObject = morphitOAuthSessionService.get(request.getOrgId());
        if (sessionObject == null) {
            String msg = String.format("Code Verifier not found for orgId: %s", request.getOrgId());
            throw new MorphitOAuth2Exception(msg);
        }

        String clientId = sessionObject.getClientId();
        String clientSecret = sessionObject.getClientSecret();
        String codeVerifier = sessionObject.getSessionId();
        if (StringUtils.isAnyBlank(clientId, clientSecret, codeVerifier)) {
            throw new MorphitOAuth2Exception("Missing PKCE data for orgId: " + orgId);
        }

        URI uri = URI.create(accessTokenEndpoint);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("secret_key", clientSecret);

            // Build Parameters
            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("grant_type", "authorization_code");
            map.add("app_id", clientId);
            map.add("code", zaloRequest.getAuthCode());
            map.add("code_verifier", codeVerifier);

            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(map,
                    headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, httpEntity,
                    String.class);
            String responseBody = responseEntity.getBody();
            if (responseBody == null) {
                throw new MorphitOAuth2Exception("Empty response from Zalo token endpoint");
            }
            if (isZaloErrorResponse(responseBody)) {
                logger.info("{}", responseBody);
                ZaloOAErrorApiResponse errorResponse = objectMapper.readValue(responseBody,
                        ZaloOAErrorApiResponse.class);
                throw new MorphitOAuth2Exception(errorResponse.getErrorName());
            }

            ZaloOATokenApiResponse apiResponse = objectMapper.readValue(responseBody, ZaloOATokenApiResponse.class);
            MorphitOAuth2TokenResponse tokenResponse = new MorphitOAuth2TokenResponse();
            tokenResponse.setClientId(clientId);
            tokenResponse.setProvider(MorphitOAuthProvider.ZaloOAuth.name());
            tokenResponse.setSourceId("");
            tokenResponse.setSourceName("");

            tokenResponse.setTokenType("access_token");
            tokenResponse.setRefreshToken(apiResponse.getRefreshToken());
            tokenResponse.setAccessToken(apiResponse.getAccessToken());

            long now = System.currentTimeMillis();
            tokenResponse.setCreatedAt(now);
            tokenResponse.setExpiredAt(now + apiResponse.getExpiredIn() * 1000L);

            return tokenResponse;
        } catch (HttpClientErrorException e) {
            String statusText = e.getStatusText();
            String responseBody = e.getResponseBodyAsString();
            logger.error("Zalo Auth Error - URI: {} - Status: {} ({}) - Body: {}", uri, e.getStatusCode().value(),
                    statusText, responseBody);
            String msg = String.format("Zalo authentication failed (HTTP %d): %s", e.getStatusCode().value(),
                    StringUtils.defaultIfBlank(responseBody, statusText));
            throw new MorphitOAuth2Exception(msg);
        } catch (Exception e) {
            logger.error("Unexpected error during Zalo authentication - URI: {}", uri, e);
            String msg = String.format("Unexpected error during Zalo authentication: %s",
                    StringUtils.defaultIfBlank(e.getMessage(), "No details"));

            throw new MorphitOAuth2Exception(msg);
        } finally {
            morphitOAuthSessionService.remove(orgId);
        }
    }

    private boolean isZaloErrorResponse(String responseText) {
        if (responseText.contains("error_name") && responseText.contains("error_reason")) {
            return true;
        }
        return false;
    }

}
