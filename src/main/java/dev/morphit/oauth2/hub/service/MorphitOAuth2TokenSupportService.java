package dev.morphit.oauth2.hub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.morphit.oauth2.hub.session.service.MorphitOAuthSessionService;

/**
* @author morphit.dee88
**/
public abstract class MorphitOAuth2TokenSupportService implements MorphitOAuth2TokenService {

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected RestTemplate restTemplate;
    
    @Autowired
    protected MorphitOAuthSessionService morphitOAuthSessionService;
    
}
