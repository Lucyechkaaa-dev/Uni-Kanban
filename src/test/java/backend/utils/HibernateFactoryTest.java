package backend.utils;

import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HibernateFactoryTest {

    @Test
    @DisplayName("Should successfully load hibernate.cfg.xml with properties")
    void testHibernateXmlConfigurationLoads() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");

        assertNotNull(configuration);
        assertEquals("org.postgresql.Driver", configuration.getProperty("hibernate.connection.driver_class"));
        assertEquals("update", configuration.getProperty("hibernate.hbm2ddl.auto"));
        assertEquals("true", configuration.getProperty("hibernate.show_sql"));
        assertEquals("10", configuration.getProperty("hibernate.connection.pool_size"));
        assertEquals("${DB_SCHEMA}", configuration.getProperty("hibernate.default_schema"));
    }
}
