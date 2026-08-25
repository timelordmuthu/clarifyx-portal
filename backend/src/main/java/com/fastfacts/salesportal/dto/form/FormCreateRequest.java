package com.fastfacts.salesportal.dto.form;

import com.fastfacts.salesportal.entity.enums.DocumentType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for Form Creation and Resubmission.
 * Contains all 31 fields defined in the BRD.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormCreateRequest {

    @NotBlank(message = "Customer Name is required")
    private String customerName;

    @NotBlank(message = "Contact Person Name is required")
    private String contactPersonName;

    @NotBlank(message = "Contact Email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be exactly 10 digits")
    private String mobileNo;

    @NotNull(message = "Document Type is required")
    private DocumentType documentType;

    @NotBlank(message = "Product Group is required")
    private String productGroup;

    @NotBlank(message = "Product Name is required")
    private String productName;

    @NotBlank(message = "Description of Charges is required")
    private String descriptionOfCharges;

    @NotBlank(message = "Invoice Type is required")
    private String invoiceType;

    private LocalDate licensePeriodFrom;
    private LocalDate licensePeriodTo;

    @NotBlank(message = "License Type is required")
    private String licenseType;

    @NotNull(message = "Billing Value is required")
    @DecimalMin(value = "0.01", message = "Billing Value must be positive")
    private BigDecimal billingValue;

    @NotBlank(message = "Payment Terms are required")
    private String paymentTerms;

    @DecimalMin(value = "0.00", message = "Advance Payment must be positive")
    private BigDecimal advancePayment;

    @NotBlank(message = "Milestone is required")
    private String milestone;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Sales Person is required")
    private String salesPerson;

    @NotBlank(message = "Sales Person Email is required")
    @Email(message = "Invalid Sales Person Email format")
    private String salesPersonEmail;

    @NotBlank(message = "Zone is required")
    private String zone;

    @NotBlank(message = "Sales Type is required")
    private String salesType;

    private String dealerEndUserName;
    private String poNumber;
    private LocalDate poDate;
    private String customerApprovalEmail;
    private LocalDate customerApprovalDate;

    // Temporary list of string paths for attachments in this phase.
    private List<String> poEmailAttachments;

    @NotBlank(message = "MSA/SLA/Proposal status is required")
    private String msaSlaProposal;

    private LocalDate msaSlaDate;

    // Temporary list of string paths for attachments in this phase.
    private List<String> msaSlaAttachments;

    private String remarks;
}
