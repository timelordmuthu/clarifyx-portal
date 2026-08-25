package com.fastfacts.salesportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FAST/FACTS Sales Approval Portal — Main Application Entry Point.
 *
 * Bootstraps the Spring Boot application with auto-configuration for:
 * - JPA/Hibernate (entity scanning from this package)
 * - Spring Security (BCrypt, RBAC)
 * - REST API controllers
 * - PostgreSQL data source
 */
@SpringBootApplication
public class FastFactsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FastFactsApplication.class, args);
    }
}
