package com.fastfacts.salesportal.entity;

import com.fastfacts.salesportal.entity.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity: Application User.
 *
 * Ref: BRD CHUNK-1 (Roles & RBAC), CHUNK-6 (DB Constraints)
 *
 * Maps to the "users" table. Supports four roles (ADMIN, SALES, CFO, FINANCE).
 * Passwords are stored as BCrypt hashes — NEVER as plaintext (SEC-001).
 * OTP fields support the password-reset flow with expiration (FLR-001).
 *
 * Key constraints:
 *   - Email must be unique (used as the login identifier).
 *   - Password column stores BCrypt-encoded values ($2a$ prefix).
 *   - Deletion is disabled by design; users are deactivated, not removed.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_users_email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Primary key — auto-generated sequence.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Full display name of the user (e.g., "Nitin Kumar").
     */
    @NotBlank(message = "User name is required")
    @Column(nullable = false)
    private String name;

    /**
     * Login email — must be unique across the system.
     * Used as the principal identifier for authentication.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * BCrypt-hashed password.
     * CRITICAL: Must NEVER store plaintext (BRD SEC-001).
     * Expected format after encoding: "$2a$10$..." or "$2y$..."
     */
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    /**
     * User role — governs access to dashboards and API endpoints.
     * Stored as the enum name string (e.g., "SALES", "CFO") in the DB.
     */
    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * One-Time Password for the password-reset flow.
     * Null when no reset is in progress.
     */
    @Column(name = "otp_code", length = 10)
    private String otpCode;

    /**
     * Expiry timestamp for the OTP.
     * After this time, the OTP is considered invalid (BRD FLR-001).
     */
    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    // =========================================================================
    // Audit Fields
    // =========================================================================

    /**
     * Timestamp of account creation — set automatically by Hibernate.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of last account modification — updated automatically.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
