package com.rdbmsdoctor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.username}")
    private String mysqlUsername;

    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String mysqlDriver;

    @Value("${h2.datasource.url}")
    private String h2Url;

    @Value("${h2.datasource.username}")
    private String h2Username;

    @Value("${h2.datasource.password}")
    private String h2Password;

    @Value("${h2.datasource.driver-class-name}")
    private String h2Driver;

    @Bean
    @Primary
    public DataSource dataSource() {
        // First try connecting to MySQL
        try {
            DriverManagerDataSource mysqlDs = new DriverManagerDataSource();
            mysqlDs.setDriverClassName(mysqlDriver);
            mysqlDs.setUrl(mysqlUrl);
            mysqlDs.setUsername(mysqlUsername);
            mysqlDs.setPassword(mysqlPassword);

            // Test connection
            try (Connection conn = mysqlDs.getConnection()) {
                log.info("Successfully connected to MySQL 8 database: {}", mysqlUrl);
                return mysqlDs;
            }
        } catch (Exception e) {
            log.warn("Could not connect to MySQL 8 database (Error: {}). Falling back to embedded H2 database (MySQL Compatibility Mode)...", e.getMessage());
        }

        // Fallback to H2 in MySQL mode
        DriverManagerDataSource h2Ds = new DriverManagerDataSource();
        h2Ds.setDriverClassName(h2Driver);
        h2Ds.setUrl(h2Url);
        h2Ds.setUsername(h2Username);
        h2Ds.setPassword(h2Password);

        // Run schema and data populators for H2
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema.sql"));
            populator.addScript(new ClassPathResource("data.sql"));
            populator.execute(h2Ds);
            log.info("Initialized H2 Database with schema.sql and data.sql successfully!");
        } catch (Exception ex) {
            log.error("Failed to populate fallback H2 database schema: ", ex);
        }

        return h2Ds;
    }
}
