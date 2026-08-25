package com.fastfacts.salesportal.entity.enums;

/**
 * Form workflow status — strict state-transition machine.
 *
 * Ref: BRD CHUNK-2 — 6-Step Workflow & State Machine
 *
 * Valid transitions:
 *   SUBMITTED       → APPROVED | REJECTED | NEED_MORE_INFO
 *   NEED_MORE_INFO  → RESUBMITTED
 *   RESUBMITTED     → APPROVED | REJECTED | NEED_MORE_INFO
 *   APPROVED        → CLOSED
 *   REJECTED        → (terminal — no further transitions)
 *   CLOSED          → (terminal — no further transitions)
 */
public enum FormStatus {

    /** Initial state — Sales Rep has submitted the request form. */
    SUBMITTED,

    /** CFO has approved the request; awaiting Finance invoice & closure. */
    APPROVED,

    /** CFO has rejected the request; workflow terminates. */
    REJECTED,

    /** CFO has requested additional information from the Sales Rep. */
    NEED_MORE_INFO,

    /** Sales Rep has edited and resubmitted after a "Need More Info" cycle. */
    RESUBMITTED,

    /** Finance Officer has uploaded the invoice and closed the workflow. */
    CLOSED
}
