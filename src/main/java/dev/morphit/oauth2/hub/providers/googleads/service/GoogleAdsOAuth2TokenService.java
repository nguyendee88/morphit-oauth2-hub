package dev.morphit.oauth2.hub.providers.googleads.service;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import dev.morphit.oauth2.hub.domain.MorphitOAuth2TokenResponse;
import dev.morphit.oauth2.hub.domain.OAuth2ConsentRequest;
import dev.morphit.oauth2.hub.domain.OAuth2RedeemTokenRequest;
import dev.morphit.oauth2.hub.exception.MorphitOAuth2Exception;
import dev.morphit.oauth2.hub.providers.MorphitOAuthProvider;
import dev.morphit.oauth2.hub.providers.googleads.domain.GoogleAdsOAuth2ConsentRequest;
import dev.morphit.oauth2.hub.providers.googleads.domain.GoogleAdsOAuth2RedeemTokenRequest;
import dev.morphit.oauth2.hub.service.MorphitOAuth2TokenSupportService;
import dev.morphit.oauth2.hub.session.domain.MorphitOAuthSessionObject;
import dev.morphit.oauth2.hub.utils.PkceUtil;
import jakarta.annotation.PostConstruct;

/**
 * @author morphit.dee88
 **/
@Service("GoogleAdsOAuth2TokenService")
public class GoogleAdsOAuth2TokenService extends MorphitOAuth2TokenSupportService {

    private Logger logger = LoggerFactory.getLogger(GoogleAdsOAuth2TokenService.class);

    @Value("${morphit.oauth2.googleads.consent.endpoint:https://accounts.google.com/o/oauth2/auth}")
    private String consentEndpoint;

    @Value("${morphit.oauth2.googleads.consent.scopes}")
    private String consentScopes;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @PostConstruct
    public void init() {
        serviceHandler.register(provider().name(), this);
    }

    @Override
    public MorphitOAuthProvider provider() {
        return MorphitOAuthProvider.GoogleAdsOAuth;
    }

    @Override
    public String buildConsentEndpoint(OAuth2ConsentRequest request) {
        GoogleAdsOAuth2ConsentRequest googleAdsRequest = (GoogleAdsOAuth2ConsentRequest) request;
        String clientId = googleAdsRequest.getClientId();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(URI.create(consentEndpoint));
        builder.queryParam("scope", consentScopes);
        builder.queryParam("response_type", "code");
        builder.queryParam("access_type", "offline");
        builder.queryParam("approval_prompt", "force");
        builder.queryParam("client_id", clientId);
        builder.queryParam("redirect_uri", googleAdsRequest.getRedirectUri());
        String sessionId = PkceUtil.generateCodeVerifier();

        MorphitOAuthSessionObject sessionObject = new MorphitOAuthSessionObject();
        sessionObject.setOrgId(request.getOrgId());
        sessionObject.setUserId(request.getUserId());
        sessionObject.setClientId(googleAdsRequest.getClientId());
        sessionObject.setClientSecret(googleAdsRequest.getClientSecret());
        sessionObject.setRedirectUri(googleAdsRequest.getRedirectUri());
        sessionObject.setProjectId(googleAdsRequest.getProjectId());
        sessionObject.setScopes(Arrays.stream(consentScopes.split(",")).map(String::trim).collect(Collectors.toList()));
        sessionObject.setSessionId(sessionId);

        return builder.build().toString();
    }

    @Override
    public MorphitOAuth2TokenResponse redeemToken(OAuth2RedeemTokenRequest request) throws MorphitOAuth2Exception {
        GoogleAdsOAuth2RedeemTokenRequest googleAdsRequest = (GoogleAdsOAuth2RedeemTokenRequest) request;
        String orgId = googleAdsRequest.getOrgId();
        String authCode = googleAdsRequest.getAuthCode();

        MorphitOAuthSessionObject sessionObject = morphitOAuthSessionService.get(orgId);
        if (sessionObject == null) {
            String msg = String.format("Code Verifier not found for orgId: %s", orgId);
            throw new MorphitOAuth2Exception(msg);
        }

        String clientId = sessionObject.getClientId();
        String clientSecret = sessionObject.getClientSecret();
        String redirectUri = sessionObject.getRedirectUri();
        List<String> scopes = sessionObject.getScopes();
        if (StringUtils.isAnyBlank(clientId, clientSecret, redirectUri)) {
            String msg = String.format("Missing app credentials for provider [%s], orgId: %s", provider(), orgId);
            throw new MorphitOAuth2Exception(msg);
        }

        if (scopes == null || scopes.isEmpty()) {
            String msg = String.format("Missing app credentials (scopes) for provider [%s], orgId: %s", provider(),
                    orgId);
            throw new MorphitOAuth2Exception(msg);
        }
        try {
            GoogleAuthorizationCodeFlow authorizationFlow = new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(), JSON_FACTORY, clientId, clientSecret, scopes).setAccessType("offline")
                    .build();
            // Authorize the OAuth2 token.
            GoogleAuthorizationCodeTokenRequest tokenRequest = authorizationFlow.newTokenRequest(authCode);
            tokenRequest.setRedirectUri(redirectUri);
            GoogleTokenResponse googleTokenResponse;
            try {
                googleTokenResponse = tokenRequest.execute();
            } catch (IOException e) {
                logger.error("Error during tokenRequest.execute() : {}", e);
                throw new MorphitOAuth2Exception("Error during tokenRequest.execute()");
            }

            MorphitOAuth2TokenResponse tokenResponse = new MorphitOAuth2TokenResponse();
            tokenResponse.setClientId(clientId);
            tokenResponse.setProjectId(sessionObject.getProjectId());
            tokenResponse.setProvider(MorphitOAuthProvider.GoogleAdsOAuth.name());
            tokenResponse.setTokenType("refresh_token");
            tokenResponse.setRefreshToken(googleTokenResponse.getRefreshToken());
            tokenResponse.setAccessToken(googleTokenResponse.getAccessToken());

            long now = System.currentTimeMillis();
            tokenResponse.setCreatedAt(now);
            tokenResponse.setExpiredAt(now + googleTokenResponse.getExpiresInSeconds() * 1000L);

            AccessToken accessToken = new AccessToken(googleTokenResponse.getAccessToken(),
                    googleTokenResponse.getExpiresInSeconds() != null
                            ? new Date(System.currentTimeMillis() + googleTokenResponse.getExpiresInSeconds() * 1000)
                            : null);
            GoogleCredentials credentials = GoogleCredentials.create(accessToken);
            Oauth2 oauth2 = new Oauth2.Builder(new NetHttpTransport(), JSON_FACTORY,
                    new HttpCredentialsAdapter(credentials)).setApplicationName(this.getClass().getSimpleName()).build();
            try {
                Userinfo userinfo = oauth2.userinfo().get().execute();
                tokenResponse.setSourceId(userinfo.getEmail());
                tokenResponse.setSourceName(userinfo.getName());
            } catch (IOException e) {
                logger.warn("Failed to fetch userinfo from Google: {}", e.getMessage());
            }

            return tokenResponse;
        } catch (Exception e) {
            logger.error("Unexpected error during GoogleAds authentication - {}", e);
            String msg = String.format("Unexpected error during GoogleAds authentication: %s",
                    StringUtils.defaultIfBlank(e.getMessage(), "No details"));

            throw new MorphitOAuth2Exception(msg);
        } finally {
            morphitOAuthSessionService.remove(orgId);
        }
    }
}
