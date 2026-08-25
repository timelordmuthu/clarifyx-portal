package com.fastfacts.salesportal.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for POST /api/auth/forgot-password.
 *
 * Triggers OTP generation for the given email address.
 * The generated OTP is saved to the user record with a 5-minute expiry window.
 *
 * Ref: BRD CHUNK-7 — FLR-001 (OTP Reset Expiration)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
