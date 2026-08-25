package com.fastfacts.salesportal.entity;

import com.fastfacts.salesportal.entity.enums.FormStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA Entity: Form Status History — Audit Trail.
 *
 * Ref: BRD CHUNK-2 (State Transitions), CHUNK-6 (DB Constraints)
 *
 * Every status change on a form MUST write a new row to this table.
 * This provides a complete, immutable audit trail of the approval workflow.
 *
 * Each record captures:
 *   - Which form changed         (form_id FK)
 *   - Who triggered the change   (changed_by FK → users)
 *   - The previous status        (nullable for initial submission)
 *   - The new status             (required)
 *   - Comments/reason            (mandatory for REJECTED & NEED_MORE_INFO)
 *   - When it happened           (timestamp)
 *
 * Rows are NEVER deleted or updated — append-only by design (BRD CHUNK-6).
 */
@Entity
@Table(name = "form_status_history", indexes = {
        @Index(name = "idx_fsh_form_id", columnList = "form_id"),
        @Index(name = "idx_fsh_changed_at", columnList = "changed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormStatusHistory {

    /**
     * Primary key — auto-generated sequence.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The form this status change belongs to.
     * Mapped as the inverse side of Form.statusHistory.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_id", nullable = false, updatable = false)
    private Form form;

    /**
     * The user who triggered this status change.
     *
     * Examples:
     *   - Sales Rep submitting/resubmitting the form
     *   - CFO approving/rejecting/requesting info
     *   - Finance Officer closing the workflow
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_id", nullable = false, updatable = false)
    private User changedBy;

    /**
     * The status BEFORE this transition.
     * NULL for the very first submission (there is no previous state).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private FormStatus previousStatus;

    /**
     * The status AFTER this transition — the target state.
     *
     * Valid transitions (enforced at service layer):
     *   null           → SUBMITTED
     *   SUBMITTED      → APPROVED | REJECTED | NEED_MORE_INFO
     *   NEED_MORE_INFO → RESUBMITTED
     *   RESUBMITTED    → APPROVED | REJECTED | NEED_MORE_INFO
     *   APPROVED       → CLOSED
     */
    @NotNull(message = "New status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private FormStatus newStatus;

    /**
     * Comments or reason for the status change.
     *
     * MANDATORY when:
     *   - CFO selects REJECTED  (must explain rejection reason)
     *   - CFO selects NEED_MORE_INFO (must explain what info is needed)
     *   - Finance closes workflow (should include ERP Invoice # and Date)
     *
     * OPTIONAL for SUBMITTED, RESUBMITTED, and APPROVED transitions.
     */
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    /**
     * Exact timestamp when this status change occurred.
     * Set once at creation — never updated (immutable audit record).
     */
    @NotNull
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    // =========================================================================
    // Lifecycle Callback
    // =========================================================================

    /**
     * Automatically sets the timestamp to the current server time
     * when the record is first persisted.
     */
    @PrePersist
    protected void onCreate() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
    }
}
