package dev.morphit.oauth2.hub.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author morphit.dee88
 * 
 */
public class HashingUtil {

    private static final Logger logger = LoggerFactory.getLogger(HashingUtil.class);

    public static String md5(String input) {
        try {
            return DigestUtils.md5Hex(input);
        } catch (Exception e) {
            logger.error("Error during MD5: {}", e.getMessage());
            return null;
        }
    }

}
