package backend.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.nio.charset.StandardCharsets;

public final class Passwords {
    public static final int MAX_BYTES = 72;
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private static final String DUMMY_HASH = ENCODER.encode("not-a-real-password");

    private Passwords() {}

    public static String hash(String raw) {
        return ENCODER.encode(raw);
    }

    public static boolean matches(String raw, String hash) {
        if (raw == null || tooLong(raw)) return false;
        if (hash == null || hash.isBlank()) {
            ENCODER.matches(raw, DUMMY_HASH);
            return false;
        }
        return ENCODER.matches(raw, hash);
    }

    public static boolean tooLong(String raw) {
        return raw.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES;
    }
}