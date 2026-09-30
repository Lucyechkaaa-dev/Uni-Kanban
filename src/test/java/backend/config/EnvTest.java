package backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvTest {

    @Test
    @DisplayName("Should load valid environment configuration with dev mode enabled")
    void testLoadDevConfig() {
        Map<String, String> env = Map.of(
                "DB_URL", "jdbc:postgresql://localhost:5432/kanban",
                "DB_USER", "postgres",
                "DB_PASS", "mysecret"
        );

        Config cfg = Env.load(env, true);

        assertNotNull(cfg);
        assertEquals("jdbc:postgresql://localhost:5432/kanban", cfg.getDbUrl());
        assertEquals("postgres", cfg.getDbUser());
        assertEquals("mysecret", cfg.getDbPass());
        assertTrue(cfg.isDev());
        assertTrue(Env.isDev());
        assertSame(cfg, Env.getConfig());
    }

    @Test
    @DisplayName("Should load valid environment configuration with dev mode disabled")
    void testLoadProdConfig() {
        Map<String, String> env = Map.of(
                "DB_URL", "jdbc:postgresql://localhost:5432/kanban",
                "DB_USER", "postgres",
                "DB_PASS", "mysecret"
        );

        Config cfg = Env.load(env, false);

        assertNotNull(cfg);
        assertFalse(cfg.isDev());
        assertFalse(Env.isDev());
    }

    @Test
    @DisplayName("Should throw informative exception when all required variables are missing")
    void testMissingAllVariables() {
        Map<String, String> emptyEnv = new HashMap<>();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> Env.load(emptyEnv, false));
        assertTrue(ex.getMessage().contains("DB_URL"));
        assertTrue(ex.getMessage().contains("DB_USER"));
        assertTrue(ex.getMessage().contains("DB_PASS"));
    }

    @Test
    @DisplayName("Should throw informative exception when single variable is missing or blank")
    void testMissingSingleVariable() {
        Map<String, String> env = Map.of(
                "DB_URL", "jdbc:postgresql://localhost:5432/kanban",
                "DB_USER", "   "
                // DB_PASS missing
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> Env.load(env, false));
        assertFalse(ex.getMessage().contains("DB_URL"));
        assertTrue(ex.getMessage().contains("DB_USER"));
        assertTrue(ex.getMessage().contains("DB_PASS"));
    }

    @Test
    @DisplayName("Should trim whitespace from loaded values")
    void testTrimsValues() {
        Map<String, String> env = Map.of(
                "DB_URL", "  jdbc:postgresql://localhost:5432/kanban  ",
                "DB_USER", "  postgres  ",
                "DB_PASS", "  secret  "
        );

        Config cfg = Env.load(env, false);
        assertEquals("jdbc:postgresql://localhost:5432/kanban", cfg.getDbUrl());
        assertEquals("postgres", cfg.getDbUser());
        assertEquals("secret", cfg.getDbPass());
    }
}
