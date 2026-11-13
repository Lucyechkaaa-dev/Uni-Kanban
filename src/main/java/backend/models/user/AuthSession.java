package backend.models.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * One login = one row. The browser holds the random token in a cookie; the table holds only its SHA-256 hash,
 * so a leaked copy of the table does not let anybody in. The rows survive a restart of the backend.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "auth_sessions", indexes = @Index(name = "idx_auth_sessions_user", columnList = "user_id"))
public class AuthSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /** Plain id instead of a relation: a session row never needs the whole User. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public AuthSession(String tokenHash, UUID userId, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }
}
