package com.fastfacts.salesportal.entity.enums;

/**
 * Document type selector for the billing request form.
 *
 * Ref: BRD CHUNK-3 — Field #5 (Document Type)
 *
 * INVOICE     → Standard billing invoice. Enables license period fields.
 * CREDIT_NOTE → Credit note against a prior invoice. Makes Remarks (Field 31) mandatory.
 */
public enum DocumentType {

    INVOICE,
    CREDIT_NOTE
}
