package com.fastfacts.salesportal.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for POST /api/forms/{id}/review (CFO endpoint).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CfoReviewRequest {

    /**
     * Action to take: APPROVED, REJECTED, or NEED_MORE_INFO.
     */
    @NotBlank(message = "Action is required")
    private String action;

    /**
     * Comments explaining the decision.
     * Mandatory if action is REJECTED or NEED_MORE_INFO.
     */
    private String comments;
}
