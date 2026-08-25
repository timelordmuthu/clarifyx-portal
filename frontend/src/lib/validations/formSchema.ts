import { z } from "zod";

export const requestFormSchema = z
  .object({
    // Section 1: Customer Information
    customerName: z
      .string()
      .min(1, "Customer name is required")
      .transform((v) => v.toUpperCase()),
    contactPersonName: z.string().min(1, "Contact person name is required"),
    contactEmail: z.string().email("Must be a valid email address"),
    mobileNo: z
      .string()
      .regex(/^\d{10}$/, "Must be a 10-digit mobile number"),

    // Section 2: Document & Product Details
    documentType: z.enum(["INVOICE", "CREDIT_NOTE"]),
    productGroup: z.string().min(1, "Product group is required"),
    productName: z.string().min(1, "Product name is required"),
    descriptionOfCharges: z.string().min(1, "Description of charges is required"),
    invoiceType: z.string().min(1, "Invoice type is required"),

    // Section 3: License Details
    licenseType: z.string().min(1, "License type is required"),
    // Dates are optional strings; cross-field validation enforces them for INVOICE
    licensePeriodFrom: z.string().optional(),
    licensePeriodTo: z.string().optional(),

    // Section 4: Billing & Payment
    // Use z.coerce.number() so HTML number inputs (which yield strings) are
    // coerced automatically — no need for { valueAsNumber: true } in register()
    billingValue: z.coerce
      .number({ invalid_type_error: "Must be a number" })
      .positive("Billing value must be greater than 0"),
    paymentTerms: z.string().min(1, "Payment terms are required"),
    advancePayment: z.coerce.number().nonnegative().optional(),
    milestone: z.string().min(1, "Milestone is required"),

    // Section 5: Sales & Geography
    state: z.string().min(1, "State is required"),
    zone: z.enum(["North", "South", "East", "West"]),
    salesPerson: z.string().min(1, "Sales person is required"),
    salesPersonEmail: z.string().email("Must be a valid email address"),
    salesType: z.enum(["Direct", "Dealer"]),
    // Optional by default; cross-field rule makes it required for Dealer
    dealerEndUserName: z.string().optional(),

    // Section 6: Purchase Order & Approvals
    poNumber: z.string().optional(),
    poDate: z.string().optional(),
    customerApprovalEmail: z.enum(["Yes", "No"]),
    // Required when customerApprovalEmail === "Yes" — enforced cross-field
    customerApprovalDate: z.string().optional(),

    // Section 7: MSA / SLA & Remarks
    msaSlaProposal: z.enum(["Yes", "No"]),
    msaSlaDate: z.string().optional(),
    remarks: z.string().optional(),
  })
  .superRefine((data, ctx) => {
    // INVOICE requires both license period dates
    // INVOICE requires both license period dates AND comparison check
    if (data.documentType === "INVOICE") {
      // 1. Check if dates exist
      if (!data.licensePeriodFrom) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ["licensePeriodFrom"],
          message: "License period from is required for invoices",
        });
      }
      if (!data.licensePeriodTo) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ["licensePeriodTo"],
          message: "License period to is required for invoices",
        });
      }

      // 2. The Comparison Logic (Test 2)
      if (data.licensePeriodFrom && data.licensePeriodTo) {
        const fromDate = new Date(data.licensePeriodFrom).getTime();
        const toDate = new Date(data.licensePeriodTo).getTime();

        if (toDate <= fromDate) {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            path: ["licensePeriodTo"],
            message: "License Period To must be after License Period From",
          });
        }
      }
    }

    // CREDIT_NOTE requires remarks
    if (data.documentType === "CREDIT_NOTE" && !data.remarks?.trim()) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["remarks"],
        message: "Remarks are mandatory for credit notes",
      });
    }

    // Dealer sales type requires dealerEndUserName
    if (data.salesType === "Dealer" && !data.dealerEndUserName?.trim()) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["dealerEndUserName"],
        message: "Dealer / end user name is required for dealer sales",
      });
    }

    // No PO number → customerApprovalEmail required to be "Yes"
    if (!data.poNumber?.trim() && data.customerApprovalEmail !== "Yes") {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["customerApprovalEmail"],
        message: "Customer approval email is required when no PO number is provided",
      });
    }

    // customerApprovalEmail "Yes" → customerApprovalDate required
    if (data.customerApprovalEmail === "Yes" && !data.customerApprovalDate) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["customerApprovalDate"],
        message: "Customer approval date is required",
      });
    }

    // MSA/SLA "Yes" → msaSlaDate required
    if (data.msaSlaProposal === "Yes" && !data.msaSlaDate) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["msaSlaDate"],
        message: "MSA / SLA date is required",
      });
    }
  });

// Derive the TypeScript type directly from the schema — single source of truth
export type RequestFormValues = z.infer<typeof requestFormSchema>;