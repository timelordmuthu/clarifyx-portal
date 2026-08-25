package com.fastfacts.salesportal.config;

import com.fastfacts.salesportal.security.CustomUserDetailsService;
import com.fastfacts.salesportal.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Configuration.
 *
 * Ref: BRD CHUNK-1 (RBAC), CHUNK-7 (SEC-001 BCrypt, SEC-002 Route Guards)
 *
 * Key features:
 *   - BCryptPasswordEncoder for password hashing (SEC-001).
 *   - Stateless JWT session management (no server-side sessions).
 *   - Role-based endpoint protection (SEC-002):
 *       /api/admin/**   → ADMIN only
 *       /api/sales/**   → SALES only
 *       /api/cfo/**     → CFO only
 *       /api/finance/** → FINANCE only
 *   - Public access to /api/auth/** endpoints (login, forgot/reset password).
 *   - CORS configured for Next.js frontend at localhost:3000.
 *   - CSRF disabled (stateless REST API with JWT auth).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // Enables @PreAuthorize, @Secured on methods
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    // =========================================================================
    // Password Encoder — BCrypt (SEC-001)
    // =========================================================================

    /**
     * BCryptPasswordEncoder bean.
     *
     * ALL passwords in the "users" table MUST be hashed with this encoder.
     * Test SEC-001 verifies: SELECT password FROM users → starts with "$2a$".
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================================================================
    // Authentication Provider
    // =========================================================================

    /**
     * DaoAuthenticationProvider wired with our custom UserDetailsService
     * and BCryptPasswordEncoder. Spring Security uses this to authenticate
     * login credentials.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the AuthenticationManager bean for use in AuthService.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // =========================================================================
    // Security Filter Chain
    // =========================================================================

    /**
     * Main security filter chain — defines URL authorization rules,
     * session policy, and filter order.
     *
     * Route guard mapping (BRD SEC-002):
     *   - Sales navigating to /api/finance/** → 403 Forbidden
     *   - Unauthenticated access to protected endpoints → 401 Unauthorized
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless REST API with JWT
            .csrf(AbstractHttpConfigurer::disable)

            // Enable CORS with our configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // URL-based authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no authentication required
                .requestMatchers(
                    "/api/auth/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // Role-based endpoint protection (BRD SEC-002)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/sales/**").hasRole("SALES")
                .requestMatchers("/api/cfo/**").hasRole("CFO")
                .requestMatchers("/api/finance/**").hasRole("FINANCE")

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // Stateless session — JWT handles authentication state
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Wire our authentication provider
            .authenticationProvider(authenticationProvider())

            // Add JWT filter BEFORE Spring's default username/password filter
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    // =========================================================================
    // CORS Configuration
    // =========================================================================

    /**
     * CORS configuration for the Next.js frontend.
     *
     * Allows requests from localhost:3000 (BRD CHUNK-8: Frontend Web App).
     * In production, restrict origins to the actual deployment domain.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins — Next.js dev server
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000"
        ));

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Allowed headers (Authorization for JWT, Content-Type for JSON)
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept"
        ));

        // Expose Authorization header to frontend JavaScript
        configuration.setExposedHeaders(List.of("Authorization"));

        // Allow cookies/credentials
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
