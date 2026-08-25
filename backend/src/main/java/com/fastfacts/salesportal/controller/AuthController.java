package com.fastfacts.salesportal.controller;

import com.fastfacts.salesportal.dto.ApiResponse;
import com.fastfacts.salesportal.dto.auth.*;
import com.fastfacts.salesportal.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller: Authentication Endpoints.
 *
 * Provides the following public endpoints (no JWT required):
 *
 *   POST /api/auth/login           → Authenticate and receive a JWT token.
 *   POST /api/auth/forgot-password → Request an OTP for password reset.
 *   POST /api/auth/reset-password  → Verify OTP and set a new password.
 *
 * Ref: BRD CHUNK-7 — SEC-001, FLR-001
 * Ref: BRD CHUNK-8 — Seed Credentials for testing
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login, password reset, and OTP management")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // =========================================================================
    // POST /api/auth/login
    // =========================================================================

    /**
     * Authenticates a user with email and password, returning a JWT token.
     *
     * On success: HTTP 200 with JWT token, user name, email, and role.
     * On failure: HTTP 401 with error message.
     *
     * Test with seed credentials (BRD CHUNK-8):
     *   - sales@fastfacts.co / Sales@0826
     *   - approver@fastfacts.co / Approver@0826
     *
     * @param request login credentials
     * @return JWT token response or error
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate with email/password and receive a JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse loginResponse = authService.login(request);

            return ResponseEntity.ok(
                    ApiResponse.success("Login successful", loginResponse)
            );
        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt for email: {}", request.getEmail());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password"));
        }
    }

    // =========================================================================
    // POST /api/auth/forgot-password
    // =========================================================================

    /**
     * Generates a 6-digit OTP for password reset and saves it to the user
     * with an expiry of exactly 5 minutes from NOW().
     *
     * In production, the OTP would be sent via email only.
     * For development/testing, the OTP is included in the response.
     *
     * @param request user's email address
     * @return success message with OTP (dev mode)
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset OTP",
            description = "Generates a 6-digit OTP valid for 5 minutes")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String otp = authService.forgotPassword(request);

            // In production, remove OTP from response — send only via email.
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "OTP has been sent to your email address. It is valid for 5 minutes.",
                            otp  // DEV ONLY: included for testing convenience
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // =========================================================================
    // POST /api/auth/reset-password
    // =========================================================================

    /**
     * Verifies the OTP and resets the user's password.
     *
     * Possible error responses (BRD FLR-001):
     *   - "The verification OTP has expired."      → OTP past 5-minute window
     *   - "Invalid OTP. Please check and try again." → OTP mismatch
     *   - "No password reset request found."         → No OTP was generated
     *
     * @param request email, OTP, and new password
     * @return success or error response
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with OTP",
            description = "Verify OTP and set a new BCrypt-hashed password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);

            return ResponseEntity.ok(
                    ApiResponse.success("Password has been reset successfully. Please login with your new password.")
            );
        } catch (RuntimeException e) {
            // Determine the appropriate HTTP status based on the error
            HttpStatus status = e.getMessage().contains("expired")
                    ? HttpStatus.GONE                   // 410 — OTP expired
                    : HttpStatus.BAD_REQUEST;           // 400 — invalid OTP or other error

            return ResponseEntity.status(status)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
