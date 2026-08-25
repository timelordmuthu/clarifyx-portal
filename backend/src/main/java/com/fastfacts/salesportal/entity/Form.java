package com.fastfacts.salesportal.entity;

import com.fastfacts.salesportal.entity.enums.DocumentType;
import com.fastfacts.salesportal.entity.enums.FormStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity: Sales Billing / Credit Note Request Form.
 *
 * Ref: BRD CHUNK-3 (Fields 1–16), CHUNK-4 (Fields 17–31),
 *      CHUNK-5 (Validation Rules), CHUNK-6 (DB Constraints)
 *
 * Maps to the "forms" table. Contains ALL 31 form fields as typed properties,
 * plus workflow metadata (status, creator, timestamps).
 *
 * Key design decisions:
 *   - File attachment paths are stored via @ElementCollection in separate
 *     collection tables ("form_po_attachments" and "form_msa_attachments").
 *   - Dropdown-sourced string fields (Product Group, State, Zone, etc.) are
 *     kept as String to allow value-list changes without schema migration.
 *   - DocumentType is an enum because it drives critical validation branching
 *     (Credit Note → mandatory Remarks).
 *   - Conditional mandatory fields are validated at the service/DTO layer,
 *     not via JPA annotations, because their requirement depends on sibling
 *     field values (e.g., Dealer Name is mandatory only when Sales Type = "Dealer").
 *   - Deletion is DISABLED by design (BRD CHUNK-6). No cascade deletes.
 */
@Entity
@Table(name = "forms", indexes = {
        @Index(name = "idx_forms_status", columnList = "current_status"),
        @Index(name = "idx_forms_created_by", columnList = "created_by_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Form {

    // =========================================================================
    // Primary Key & Workflow Metadata
    // =========================================================================

    /**
     * Primary key — auto-generated sequence.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The Sales Rep who originally created this request.
     * Immutable after initial submission.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy;

    /**
     * Current workflow status — driven by the state machine.
     * Valid transitions are enforced at the service layer.
     *
     * Ref: BRD CHUNK-2 (State Transitions)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 20)
    private FormStatus currentStatus;

    // =========================================================================
    // FIELD 1–4: Customer Information
    // Ref: BRD CHUNK-3
    // =========================================================================

    /**
     * Field 1: Customer Name.
     * MUST be ALL CAPS to match GSTIN registry (BRD VAL-001).
     * Validated at the service/DTO layer with a regex pattern.
     */
    @NotBlank(message = "Customer Name is required")
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    /**
     * Field 2: Contact Person Name.
     * Name of the invoice recipient at the customer's organization.
     */
    @NotBlank(message = "Contact Person Name is required")
    @Column(name = "contact_person_name", nullable = false)
    private String contactPersonName;

    /**
     * Field 3: Contact Email.
     * Must be a valid email format (name@domain.com).
     */
    @NotBlank(message = "Contact Email is required")
    @Email(message = "Invalid email format")
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    /**
     * Field 4: Mobile Number.
     * Exactly 10 digits — no country codes, spaces, or special characters.
     * Stored as String to preserve leading zeros and avoid numeric truncation.
     */
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be exactly 10 digits")
    @Column(name = "mobile_no", nullable = false, length = 10)
    private String mobileNo;

    // =========================================================================
    // FIELD 5–9: Document & Product Details
    // Ref: BRD CHUNK-3
    // =========================================================================

    /**
     * Field 5: Document Type.
     * Enum: INVOICE or CREDIT_NOTE.
     * Drives conditional logic:
     *   - CREDIT_NOTE → Remarks (Field 31) becomes mandatory.
     *   - INVOICE → License period fields (10, 11) may become mandatory.
     */
    @NotNull(message = "Document Type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private DocumentType documentType;

    /**
     * Field 6: Product Group.
     * Dropdown options: Desktop, eTds Wizard, Web FAMS, Web PayPac, Web TDS.
     * Drives dynamic filtering of Product Name (Field 7).
     */
    @NotBlank(message = "Product Group is required")
    @Column(name = "product_group", nullable = false)
    private String productGroup;

    /**
     * Field 7: Product Name.
     * Dynamically filtered based on Product Group (Field 6).
     * The valid product names per group are maintained in the frontend/config.
     */
    @NotBlank(message = "Product Name is required")
    @Column(name = "product_name", nullable = false)
    private String productName;

    /**
     * Field 8: Description of Charges.
     * Dropdown options: License Fee, AMC, Subscription, Professional Fee, etc.
     */
    @NotBlank(message = "Description of Charges is required")
    @Column(name = "description_of_charges", nullable = false)
    private String descriptionOfCharges;

    /**
     * Field 9: Invoice Type.
     * Derived from Document Type. Options: New Sales, Renewals, Recurring, Support.
     */
    @NotBlank(message = "Invoice Type is required")
    @Column(name = "invoice_type", nullable = false)
    private String invoiceType;

    // =========================================================================
    // FIELD 10–12: License Details
    // Ref: BRD CHUNK-3
    // =========================================================================

    /**
     * Field 10: License Period From (Start Date).
     * MANDATORY if Document Type = INVOICE and License Type is active.
     * Format: DD/MM/YYYY (stored as LocalDate, formatted at the API layer).
     */
    @Column(name = "license_period_from")
    private LocalDate licensePeriodFrom;

    /**
     * Field 11: License Period To (End Date).
     * MANDATORY if Document Type = INVOICE.
     * Must be chronologically AFTER Field 10 (BRD VAL-002).
     */
    @Column(name = "license_period_to")
    private LocalDate licensePeriodTo;

    /**
     * Field 12: License Type.
     * Options: "Perpetual (AMC)" or "Subscription (SaaS)".
     */
    @NotBlank(message = "License Type is required")
    @Column(name = "license_type", nullable = false)
    private String licenseType;

    // =========================================================================
    // FIELD 13–16: Billing & Payment
    // Ref: BRD CHUNK-3
    // =========================================================================

    /**
     * Field 13: Billing Value (INR).
     * Net amount — must be a positive number.
     * Uses BigDecimal for financial precision (avoids floating-point errors).
     */
    @NotNull(message = "Billing Value is required")
    @DecimalMin(value = "0.01", message = "Billing Value must be a positive amount")
    @Column(name = "billing_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal billingValue;

    /**
     * Field 14: Payment Terms.
     * Based on PO/SLA terms (e.g., "30 Days Credit", "Immediate").
     */
    @NotBlank(message = "Payment Terms are required")
    @Column(name = "payment_terms", nullable = false)
    private String paymentTerms;

    /**
     * Field 15: Advance Payment (INR).
     * OPTIONAL. Must be a positive number if provided.
     */
    @DecimalMin(value = "0.00", message = "Advance Payment must be a positive amount")
    @Column(name = "advance_payment", precision = 15, scale = 2)
    private BigDecimal advancePayment;

    /**
     * Field 16: Milestone.
     * Payment trigger description (e.g., "100% on delivery", "50-50 split").
     */
    @NotBlank(message = "Milestone is required")
    @Column(name = "milestone", nullable = false)
    private String milestone;

    // =========================================================================
    // FIELD 17–22: Sales & Geography
    // Ref: BRD CHUNK-4
    // =========================================================================

    /**
     * Field 17: State (Indian State).
     * Governs GST computation: same-state → CGST+SGST, inter-state → IGST.
     * Populated from a dropdown of all Indian states/UTs.
     */
    @NotBlank(message = "State is required")
    @Column(name = "state", nullable = false)
    private String state;

    /**
     * Field 18: Sales Person.
     * Auto-filled with the logged-in user's name, but editable.
     */
    @NotBlank(message = "Sales Person is required")
    @Column(name = "sales_person", nullable = false)
    private String salesPerson;

    /**
     * Field 19: Sales Person Email.
     * Auto-filled from the logged-in user's email.
     */
    @NotBlank(message = "Sales Person Email is required")
    @Email(message = "Invalid Sales Person Email format")
    @Column(name = "sales_person_email", nullable = false)
    private String salesPersonEmail;

    /**
     * Field 20: Zone.
     * Options: North, South, East, West.
     */
    @NotBlank(message = "Zone is required")
    @Column(name = "zone", nullable = false, length = 10)
    private String zone;

    /**
     * Field 21: Sales Type.
     * Options: "Direct" or "Dealer".
     * Dictates commission logic and controls Field 22 behavior:
     *   - Direct → Field 22 auto-copies Customer Name and is disabled.
     *   - Dealer → Field 22 is enabled, mandatory, and empty.
     */
    @NotBlank(message = "Sales Type is required")
    @Column(name = "sales_type", nullable = false, length = 10)
    private String salesType;

    /**
     * Field 22: Dealer / End User Name.
     * MANDATORY if Sales Type = "Dealer" (BRD VAL-003).
     * If Sales Type = "Direct", this auto-copies Customer Name (Field 1).
     */
    @Column(name = "dealer_end_user_name")
    private String dealerEndUserName;

    // =========================================================================
    // FIELD 23–27: Purchase Order / Approval Proof
    // Ref: BRD CHUNK-4
    // =========================================================================

    /**
     * Field 23: PO Number.
     * MANDATORY if PO is chosen as the approval proof mechanism.
     * Mutually linked with Fields 24–26 (PO path vs Email Approval path).
     */
    @Column(name = "po_number")
    private String poNumber;

    /**
     * Field 24: PO Date.
     * MANDATORY if PO Number (Field 23) is provided.
     */
    @Column(name = "po_date")
    private LocalDate poDate;

    /**
     * Field 25: Customer Approval Email confirmation.
     * MANDATORY if PO Number is empty — acts as alternative approval proof.
     * Stores "Yes" or null/blank.
     */
    @Column(name = "customer_approval_email")
    private String customerApprovalEmail;

    /**
     * Field 26: Customer Approval Date.
     * MANDATORY if Field 25 = "Yes".
     */
    @Column(name = "customer_approval_date")
    private LocalDate customerApprovalDate;

    /**
     * Field 27: PO / Email Attachment file paths.
     * MANDATORY — at least one attachment is required.
     * Supports: PDF, DOCX, Images. Max 5 files.
     * Stored as a list of server-side file paths in a collection table.
     */
    @ElementCollection
    @CollectionTable(
            name = "form_po_attachments",
            joinColumns = @JoinColumn(name = "form_id")
    )
    @Column(name = "file_path", nullable = false)
    @Builder.Default
    private List<String> poEmailAttachments = new ArrayList<>();

    // =========================================================================
    // FIELD 28–30: MSA / SLA / Proposal
    // Ref: BRD CHUNK-4
    // =========================================================================

    /**
     * Field 28: MSA / SLA / Proposal indicator.
     * Options: "Yes" or "No".
     * If "Yes" → Fields 29 and 30 become mandatory.
     */
    @NotBlank(message = "MSA/SLA/Proposal status is required")
    @Column(name = "msa_sla_proposal", nullable = false, length = 5)
    private String msaSlaProposal;

    /**
     * Field 29: MSA / SLA Date.
     * MANDATORY if Field 28 = "Yes".
     */
    @Column(name = "msa_sla_date")
    private LocalDate msaSlaDate;

    /**
     * Field 30: MSA / SLA Attachment file paths.
     * MANDATORY if Field 28 = "Yes".
     * Supports: PDF, DOCX, Images. Max 5 files.
     */
    @ElementCollection
    @CollectionTable(
            name = "form_msa_attachments",
            joinColumns = @JoinColumn(name = "form_id")
    )
    @Column(name = "file_path")
    @Builder.Default
    private List<String> msaSlaAttachments = new ArrayList<>();

    // =========================================================================
    // FIELD 31: Remarks / Comments
    // Ref: BRD CHUNK-4
    // =========================================================================

    /**
     * Field 31: Remarks / Comments.
     * OPTIONAL for Invoice document types.
     * MANDATORY if Document Type = CREDIT_NOTE — must reference the original
     * invoice details (BRD VAL-004).
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // =========================================================================
    // Relationships
    // =========================================================================

    /**
     * Complete audit trail of all status transitions for this form.
     * Ordered chronologically. Cascade ALL so history rows are persisted
     * when the form is saved. Orphan removal ensures consistency.
     *
     * Ref: BRD CHUNK-6 — "Every status change writes a new row to form_status_history"
     */
    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("changedAt ASC")
    @Builder.Default
    private List<FormStatusHistory> statusHistory = new ArrayList<>();

    // =========================================================================
    // Audit Fields
    // =========================================================================

    /**
     * Timestamp of initial form submission — set automatically.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of last modification (e.g., resubmission) — updated automatically.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================================================================
    // Convenience Methods
    // =========================================================================

    /**
     * Adds a new status history entry and updates the form's current status.
     * This is the ONLY way status should be changed — ensures the audit trail
     * is always written (BRD CHUNK-6 requirement).
     *
     * @param history the status transition record to append
     */
    public void addStatusHistory(FormStatusHistory history) {
        statusHistory.add(history);
        history.setForm(this);
        this.currentStatus = history.getNewStatus();
    }
}
