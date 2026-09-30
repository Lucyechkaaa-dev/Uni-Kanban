package backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    @DisplayName("Should create config via builder and access via standard getters")
    void testConfigAccessors() {
        Config config = Config.builder()
                .dbUrl("jdbc:postgresql://localhost:5432/testdb")
                .dbUser("admin")
                .dbPass("secret123")
                .dbSchema("custom_schema")
                .dev(true)
                .build();

        assertEquals("jdbc:postgresql://localhost:5432/testdb", config.getDbUrl());
        assertEquals("admin", config.getDbUser());
        assertEquals("secret123", config.getDbPass());
        assertEquals("custom_schema", config.getDbSchema());
        assertTrue(config.isDev());
    }

    @Test
    @DisplayName("toString should not expose plain text database password")
    void testToStringMasksPassword() {
        Config config = Config.builder()
                .dbUrl("jdbc:postgresql://localhost:5432/testdb")
                .dbUser("admin")
                .dbPass("superSecretPassword!")
                .dev(false)
                .build();

        String str = config.toString();
        assertFalse(str.contains("superSecretPassword!"), "Password should be masked in toString");
        assertTrue(str.contains("[HIDDEN]"), "Password should be replaced with [HIDDEN]");
    }
}
