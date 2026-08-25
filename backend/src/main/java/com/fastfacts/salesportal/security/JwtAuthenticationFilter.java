package com.fastfacts.salesportal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 *
 * Intercepts every HTTP request, extracts the JWT from the Authorization header,
 * validates it, and sets the Spring Security authentication context.
 *
 * Flow:
 *   1. Read "Authorization: Bearer <token>" header.
 *   2. Extract email (subject) from the token.
 *   3. Load UserDetails from the database.
 *   4. Validate the token against UserDetails.
 *   5. Set the SecurityContext so downstream filters/controllers see the
 *      authenticated principal with its role-based authorities.
 *
 * Requests without a valid JWT proceed unauthenticated — the SecurityConfig
 * determines which endpoints are publicly accessible.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Step 1: Extract the Authorization header
            final String authHeader = request.getHeader("Authorization");

            // Skip if no Bearer token is present
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Step 2: Parse the JWT token
            final String jwt = authHeader.substring(BEARER_PREFIX.length());
            final String userEmail = jwtUtil.extractEmail(jwt);

            // Step 3: Authenticate if not already set in context
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Step 4: Validate token against loaded user
                if (jwtUtil.validateToken(jwt, userDetails)) {

                    // Step 5: Create authentication token and set context
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authenticated user: {} with role: {}",
                            userEmail, jwtUtil.extractRole(jwt));
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
