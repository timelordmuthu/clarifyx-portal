package com.fastfacts.salesportal.config;

import com.fastfacts.salesportal.entity.User;
import com.fastfacts.salesportal.entity.enums.Role;
import com.fastfacts.salesportal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Database Seeder — Seeds default users on application startup.
 *
 * Ref: BRD CHUNK-8 — Standard QA Seed Credentials
 *
 * Creates the 4 required test users with BCrypt-hashed passwords:
 *
 *   | Role           | Email                  | Password (raw)  |
 *   |----------------|------------------------|-----------------|
 *   | ADMIN          | muthu@fastfacts.co     | Betty@0826      |
 *   | SALES          | sales@fastfacts.co     | Sales@0826      |
 *   | CFO            | approver@fastfacts.co  | Approver@0826   |
 *   | FINANCE        | finance@fastfacts.co   | Finance@0826    |
 *
 * Idempotent: Checks existsByEmail() before inserting to prevent duplicates
 * on application restart. Passwords are ALWAYS hashed via BCryptPasswordEncoder
 * before storage (SEC-001 compliance).
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        logger.info("========================================");
        logger.info("  DataLoader: Seeding default users...");
        logger.info("========================================");

        // =====================================================================
        // User 1: System Administrator — Divya S (muthu@fastfacts.co)
        // =====================================================================
        seedUser(
                "Divya S",
                "muthu@fastfacts.co",
                "Betty@0826",
                Role.ADMIN
        );

        // =====================================================================
        // User 2: Sales Representative — Nitin (sales@fastfacts.co)
        // =====================================================================
        seedUser(
                "Nitin",
                "sales@fastfacts.co",
                "Sales@0826",
                Role.SALES
        );

        // =====================================================================
        // User 3: CFO Approver — Balaji (approver@fastfacts.co)
        // =====================================================================
        seedUser(
                "Balaji",
                "approver@fastfacts.co",
                "Approver@0826",
                Role.CFO
        );

        // =====================================================================
        // User 4: Finance Officer — Abhishek (finance@fastfacts.co)
        // =====================================================================
        seedUser(
                "Abhishek",
                "finance@fastfacts.co",
                "Finance@0826",
                Role.FINANCE
        );

        logger.info("========================================");
        logger.info("  DataLoader: Seeding complete.");
        logger.info("========================================");
    }

    /**
     * Seeds a single user into the database if they don't already exist.
     *
     * @param name        user's display name
     * @param email       login email (unique identifier)
     * @param rawPassword plaintext password — will be BCrypt-hashed before storage
     * @param role        user's RBAC role
     */
    private void seedUser(String name, String email, String rawPassword, Role role) {
        if (userRepository.existsByEmail(email)) {
            logger.info("  [SKIP] User already exists: {} ({})", email, role);
            return;
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))  // BCrypt hash (SEC-001)
                .role(role)
                .build();

        userRepository.save(user);
        logger.info("  [SEED] Created user: {} ({}) — {}", name, email, role);
    }
}
