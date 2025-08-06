package dev.morphit.oauth2.hub.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import dev.morphit.oauth2.hub.exception.MorphitOAuth2ProviderUnsupported;

/**
 * @author morphit.dee88
 **/
@Service
public class MorphitOAuth2TokenServiceHandler {

    private Map<String, MorphitOAuth2TokenService> providers = new HashMap<>();

    public void register(String provider, MorphitOAuth2TokenService service) {
        providers.put(provider, service);
    }

    public MorphitOAuth2TokenService getService(String provider) throws MorphitOAuth2ProviderUnsupported {
        MorphitOAuth2TokenService service = providers.get(provider);
        if (service == null) {
            String msg = String.format("Provider %s has not been supported yet.", provider);
            throw new MorphitOAuth2ProviderUnsupported(msg);
        }
        return service;
    }
}
