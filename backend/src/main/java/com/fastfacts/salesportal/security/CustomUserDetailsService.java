package com.fastfacts.salesportal.security;

import com.fastfacts.salesportal.entity.User;
import com.fastfacts.salesportal.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation for Spring Security.
 *
 * Loads user details from the PostgreSQL "users" table via JPA.
 * Maps the application's Role enum to a Spring Security GrantedAuthority
 * with the "ROLE_" prefix (e.g., ROLE_SALES, ROLE_CFO).
 *
 * Ref: BRD CHUNK-1 — Roles & Access Control (RBAC)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user by email (the login identifier in our system).
     *
     * @param email the user's email address
     * @return Spring Security UserDetails with role-based authority
     * @throws UsernameNotFoundException if no user is found for this email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        // Map our Role enum to Spring Security's "ROLE_<NAME>" convention.
        // This enables @PreAuthorize("hasRole('CFO')") and similar checks.
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                "ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
}
