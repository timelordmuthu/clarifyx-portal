package com.fastfacts.salesportal.entity.enums;

/**
 * User roles for Role-Based Access Control (RBAC).
 *
 * Ref: BRD CHUNK-1 — User Roles & Access Control
 *
 * ADMIN   → System Administrator (Divya S) — manages users, resets passwords.
 * SALES   → Sales Representative (Nitin / team) — creates & resubmits requests.
 * CFO     → CFO Approver (Balaji) — approves, rejects, or requests more info.
 * FINANCE → Finance Officer (Nitin / Abhishek) — uploads invoices, closes workflow.
 */
public enum Role {

    ADMIN,
    SALES,
    CFO,
    FINANCE
}
