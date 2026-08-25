package com.fastfacts.salesportal.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for POST /api/forms/{id}/close (Finance endpoint).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceCloseRequest {

    /**
     * Comments for closing the workflow (should include ERP Invoice # and Date).
     */
    @NotBlank(message = "Comments are required (must include ERP Invoice details)")
    private String comments;
}
