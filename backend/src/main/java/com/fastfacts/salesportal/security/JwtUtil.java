package com.fastfacts.salesportal.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Token Utility.
 *
 * Handles all JWT operations: token generation, validation, and claim extraction.
 * Uses HMAC-SHA256 (HS256) signing with a configurable secret key.
 *
 * Token payload includes:
 *   - sub   : user email (principal identifier)
 *   - role  : user's RBAC role (ADMIN, SALES, CFO, FINANCE)
 *   - name  : user's display name
 *   - iat   : issued-at timestamp
 *   - exp   : expiration timestamp (default 24h, configurable)
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey signingKey;
    private final long expirationMs;

    /**
     * Constructs the JWT utility with values from application.yml.
     *
     * @param secret     JWT signing secret (must be ≥256 bits for HS256)
     * @param expiration token validity duration in milliseconds
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expiration;
    }

    // =========================================================================
    // Token Generation
    // =========================================================================

    /**
     * Generates a JWT token for the authenticated user.
     *
     * @param email user's email (becomes the token subject)
     * @param role  user's RBAC role (stored as a custom claim)
     * @param name  user's display name (stored as a custom claim)
     * @return signed JWT token string
     */
    public String generateToken(String email, String role, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("name", name);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    // =========================================================================
    // Token Validation
    // =========================================================================

    /**
     * Validates a JWT token against the provided UserDetails.
     * Checks that:
     *   1. The token's subject matches the UserDetails username (email).
     *   2. The token has not expired.
     *   3. The token signature is valid.
     *
     * @param token       the JWT token string
     * @param userDetails the authenticated user's details
     * @return true if the token is valid for this user
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // Claim Extraction
    // =========================================================================

    /**
     * Extracts the user's email (subject) from the token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the user's role from the token's custom claims.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Extracts the token's expiration date.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claim extractor using a resolver function.
     *
     * @param token          the JWT token
     * @param claimsResolver function to extract a specific claim
     * @param <T>            the claim type
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // =========================================================================
    // Internal Helpers
    // =========================================================================

    /**
     * Parses and returns all claims from the JWT token.
     * Throws JwtException if the signature is invalid or the token is malformed.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks whether the token has passed its expiration timestamp.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
