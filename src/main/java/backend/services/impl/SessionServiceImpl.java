package backend.services.impl;

import backend.dao.AuthSessionDao;
import backend.models.user.AuthSession;
import backend.services.SessionService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionServiceImpl implements SessionService {

    private final AuthSessionDao dao;
    private final SecureRandom random = new SecureRandom();

    public SessionServiceImpl(AuthSessionDao dao) {
        this.dao = dao;
    }

    @Override
    public String open(UUID userId) {
        byte[] bytes = new byte[32]; // 256 random bits: impossible to guess
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        dao.deleteExpired(Instant.now()); // cheap cleaning on every login, no scheduler needed
        dao.save(new AuthSession(hash(token), userId, Instant.now().plus(LIFETIME)));
        return token;
    }

    @Override
    public Optional<UUID> userIdOf(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String hash = hash(token);
        return dao.findByTokenHash(hash).flatMap(found -> {
            if (found.getExpiresAt().isBefore(Instant.now())) {
                dao.deleteByTokenHash(hash);
                return Optional.empty();
            }
            return Optional.of(found.getUserId());
        });
    }

    @Override
    public void close(String token) {
        if (token != null && !token.isBlank()) {
            dao.deleteByTokenHash(hash(token));
        }
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest); // 64 characters
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is part of every JVM", e);
        }
    }
}
