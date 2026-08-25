package com.fastfacts.salesportal.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the login response payload.
 *
 * Returns the JWT token along with user profile metadata so the
 * frontend can store the token and render role-appropriate dashboards.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    /** JWT access token — include in Authorization header as "Bearer <token>". */
    private String token;

    /** Authenticated user's display name. */
    private String name;

    /** Authenticated user's email (login identifier). */
    private String email;

    /** Authenticated user's role — determines which dashboard to render. */
    private String role;
}
