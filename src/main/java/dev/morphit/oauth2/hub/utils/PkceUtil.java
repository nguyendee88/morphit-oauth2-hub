package dev.morphit.oauth2.hub.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
* @author morphit.dee88
 */
public class PkceUtil {

    /**
     * Generate a secure random code_verifier string
     */
    public static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifierBytes = new byte[32];
        secureRandom.nextBytes(codeVerifierBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);
    }

    /**
     * Generate code_challenge (SHA256 hash of code_verifier)
     * 
     * @param codeVerifier original verifier string
     * @return code_challenge string (Base64Url encoded SHA256)
     */
    public static String generateCodeChallenge(String codeVerifier) {
        if (codeVerifier == null) {
            throw new IllegalArgumentException("codeVerifier must not be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error generating PKCE code_challenge", e);
        }
    }

}
