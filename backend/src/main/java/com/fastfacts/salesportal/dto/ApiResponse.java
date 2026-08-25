package com.fastfacts.salesportal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper.
 *
 * Provides a consistent JSON structure for all API responses:
 * {
 *   "success": true/false,
 *   "message": "Human-readable message",
 *   "data": { ... }    // optional payload
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** Whether the operation succeeded. */
    private boolean success;

    /** Human-readable status/error message. */
    private String message;

    /** Optional data payload (login response, form data, etc.). */
    private T data;

    // =========================================================================
    // Convenience Factory Methods
    // =========================================================================

    /**
     * Creates a success response with a message and data payload.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates a success response with only a message (no data payload).
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Creates an error response with a message.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
