package com.fastfacts.salesportal.repository;

import com.fastfacts.salesportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository: User data access.
 *
 * Provides standard CRUD plus custom queries for authentication
 * and user management workflows.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address (login identifier).
     * Used by: AuthService (login), CustomUserDetailsService, OTP flow.
     *
     * @param email the user's email (case-sensitive)
     * @return the User if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email already exists.
     * Used by: DataLoader (seed idempotency), Admin user registration.
     *
     * @param email the email to check
     * @return true if a user with this email exists
     */
    boolean existsByEmail(String email);
}
