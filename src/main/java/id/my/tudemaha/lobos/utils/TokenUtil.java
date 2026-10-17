package id.my.tudemaha.lobos.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenUtil {
    private static final SecureRandom secureRandom = new SecureRandom();
    public static String generate(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }
}
