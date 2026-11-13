package backend.services;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/** Logins of the web API: a random token that the browser keeps in a cookie and the database keeps as a hash. */
public interface SessionService {

    String COOKIE_NAME = "KANBAN_SESSION";

    /** How long one login lasts. */
    Duration LIFETIME = Duration.ofDays(7);

    /** Remembers a new login of the user and returns its token (the only moment the token exists in clear text). */
    String open(UUID userId);

    /** Who owns this token; empty if it is unknown, expired or already logged out. */
    Optional<UUID> userIdOf(String token);

    /** Logout: the token stops working at once. Unknown tokens are ignored. */
    void close(String token);
}
