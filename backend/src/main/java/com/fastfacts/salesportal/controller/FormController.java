package com.fastfacts.salesportal.controller;

import com.fastfacts.salesportal.dto.ApiResponse;
import com.fastfacts.salesportal.dto.form.CfoReviewRequest;
import com.fastfacts.salesportal.dto.form.FinanceCloseRequest;
import com.fastfacts.salesportal.dto.form.FormCreateRequest;
import com.fastfacts.salesportal.entity.Form;
import com.fastfacts.salesportal.service.FormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forms")
@Tag(name = "Forms", description = "Endpoints for Form Creation and Approval Workflow")
public class FormController {

    private final FormService formService;

    public FormController(FormService formService) {
        this.formService = formService;
    }

    /**
     * POST /api/forms
     * Only SALES can trigger this.
     */
    @PostMapping
    @PreAuthorize("hasRole('SALES')")
    @Operation(summary = "Create a new form", description = "Sales role creates a form, state becomes SUBMITTED")
    public ResponseEntity<ApiResponse<Form>> createForm(
            @Valid @RequestBody FormCreateRequest request,
            Authentication authentication) {
        try {
            Form form = formService.createForm(request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Form submitted successfully.", form));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/forms/{id}/review
     * Only CFO can trigger this.
     */
    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('CFO')")
    @Operation(summary = "CFO Review Form", description = "CFO reviews a form (APPROVE, REJECT, NEED_MORE_INFO)")
    public ResponseEntity<ApiResponse<Form>> reviewForm(
            @PathVariable Long id,
            @Valid @RequestBody CfoReviewRequest request,
            Authentication authentication) {
        try {
            Form form = formService.cfoReview(id, request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Form reviewed successfully.", form));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/forms/{id}/resubmit
     * Only SALES can trigger this.
     */
    @PutMapping("/{id}/resubmit")
    @PreAuthorize("hasRole('SALES')")
    @Operation(summary = "Resubmit a form", description = "Sales resubmits a NEED_MORE_INFO form, state becomes RESUBMITTED")
    public ResponseEntity<ApiResponse<Form>> resubmitForm(
            @PathVariable Long id,
            @Valid @RequestBody FormCreateRequest request,
            Authentication authentication) {
        try {
            Form form = formService.resubmitForm(id, request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Form resubmitted successfully.", form));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/forms/{id}/close
     * Only FINANCE can trigger this.
     */
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Finance Close Form", description = "Finance closes an APPROVED form")
    public ResponseEntity<ApiResponse<Form>> closeForm(
            @PathVariable Long id,
            @Valid @RequestBody FinanceCloseRequest request,
            Authentication authentication) {
        try {
            Form form = formService.financeClose(id, request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Form closed successfully.", form));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
