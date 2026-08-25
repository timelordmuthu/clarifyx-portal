package com.fastfacts.salesportal.service;

import com.fastfacts.salesportal.dto.auth.*;
import com.fastfacts.salesportal.entity.User;
import com.fastfacts.salesportal.repository.UserRepository;
import com.fastfacts.salesportal.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Authentication Service — handles login, OTP generation, and password reset.
 *
 * Ref: BRD CHUNK-7 — SEC-001 (BCrypt), FLR-001 (OTP Expiry)
 *
 * Business rules:
 *   - Login: Authenticate via BCrypt-hashed password, return JWT on success.
 *   - Forgot Password: Generate a 6-digit OTP, store with 5-minute expiry.
 *   - Reset Password: Verify OTP is valid and not expired, then BCrypt-hash
 *     the new password and clear the OTP fields.
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /** OTP validity window — exactly 5 minutes from generation (BRD FLR-001). */
    private static final int OTP_EXPIRY_MINUTES = 5;

    /** OTP digit count. */
    private static final int OTP_LENGTH = 6;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // =========================================================================
    // Login
    // =========================================================================

    /**
     * Authenticates the user and returns a JWT token with role metadata.
     *
     * @param request login credentials (email + password)
     * @return LoginResponse containing JWT, user name, email, and role
     * @throws BadCredentialsException if email/password combo is invalid
     */
    public LoginResponse login(LoginRequest request) {
        // Delegate to Spring Security's AuthenticationManager for BCrypt verification
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Authentication succeeded — load user for token generation
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // Generate JWT with role and name claims
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getName()
        );

        logger.info("User logged in successfully: {} [{}]", user.getEmail(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    // =========================================================================
    // Forgot Password — OTP Generation
    // =========================================================================

    /**
     * Generates a 6-digit OTP for the given email and stores it with
     * a 5-minute expiry window.
     *
     * In production, the OTP would be sent via email (Spring Boot Mail).
     * For development/testing, the OTP is logged and returned in the response.
     *
     * @param request contains the user's email
     * @return the generated OTP (for dev/testing visibility)
     * @throws RuntimeException if no user is found for the email
     */
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "No account found with email: " + request.getEmail()));

        // Generate a cryptographically random 6-digit OTP
        String otp = generateOtp();

        // Store OTP with expiry = NOW() + 5 minutes (BRD FLR-001)
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        userRepository.save(user);

        logger.info("OTP generated for user: {} (expires at: {})",
                user.getEmail(), user.getOtpExpiry());

        // TODO: Send OTP via email using Spring Boot Mail
        // emailService.sendOtpEmail(user.getEmail(), otp);

        return otp;
    }

    // =========================================================================
    // Reset Password — OTP Verification
    // =========================================================================

    /**
     * Verifies the OTP and resets the user's password.
     *
     * Validation checks (in order):
     *   1. User exists for the given email.
     *   2. An OTP has been generated (not null).
     *   3. The OTP has NOT expired (BRD FLR-001 — exact message required).
     *   4. The OTP value matches.
     *   5. New password is BCrypt-hashed before storage.
     *
     * After successful reset, OTP fields are cleared to prevent reuse.
     *
     * @param request contains email, OTP, and new password
     * @throws RuntimeException with specific messages per validation failure
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Step 1: Find the user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "No account found with email: " + request.getEmail()));

        // Step 2: Verify OTP exists
        if (user.getOtpCode() == null) {
            throw new RuntimeException(
                    "No password reset request found. Please request a new OTP.");
        }

        // Step 3: Check OTP expiry (BRD FLR-001)
        // EXACT error message required by test case: "The verification OTP has expired."
        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            // Clear expired OTP to prevent retry attacks
            user.setOtpCode(null);
            user.setOtpExpiry(null);
            userRepository.save(user);

            throw new RuntimeException("The verification OTP has expired.");
        }

        // Step 4: Verify OTP value matches
        if (!user.getOtpCode().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP. Please check and try again.");
        }

        // Step 5: Hash the new password with BCrypt and save
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // Clear OTP fields — single-use only
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        logger.info("Password reset successful for user: {}", user.getEmail());
    }

    // =========================================================================
    // Internal Helpers
    // =========================================================================

    /**
     * Generates a cryptographically random 6-digit OTP string.
     * Uses SecureRandom for unpredictability.
     *
     * @return 6-digit OTP (e.g., "048271", "935610")
     */
    private String generateOtp() {
        int otp = secureRandom.nextInt(900000) + 100000; // Range: 100000–999999
        return String.valueOf(otp);
    }
}
