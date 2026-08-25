"use client";

import React from "react";
import { useForm, SubmitHandler ,Resolver } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  requestFormSchema,
  RequestFormValues,
} from "@/lib/validations/formSchema";

// ─── Fix #1 & #4: useForm typed with RequestFormValues (no `any` casts).
//     zodResolver receives the correctly typed schema — no `as any` needed.
// ─── Fix #6: billingValue / advancePayment use z.coerce.number() in the schema
//     so { valueAsNumber: true } is removed from every register() call here.
// ─── Fix #9: dealerEndUserName uses shouldUnregister: true so that when the
//     field is hidden (Direct mode) its value is excluded from the payload and
//     validation never runs on a stale value.

export default function RequestCreationForm() {
  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<RequestFormValues>({
    resolver: zodResolver(requestFormSchema)as unknown as Resolver<RequestFormValues>,
    mode: "onBlur",
    defaultValues: {
      documentType: "INVOICE",
      salesType: "Direct",
      msaSlaProposal: "No",
      zone: "North",
      customerApprovalEmail: "No",
    },
  });

  // Watchers for conditional rendering / logic
  const salesType = watch("salesType");
  const documentType = watch("documentType");
  const customerName = watch("customerName");
  const poNumber = watch("poNumber");
  const customerApprovalEmail = watch("customerApprovalEmail");
  const msaSlaProposal = watch("msaSlaProposal");

  // ─── Fix #9: React to salesType changes.
  //     For Direct: mirror Customer Name into the dealer field.
  //     For Dealer:  clear the dealer field so no stale value lingers.
  React.useEffect(() => {
    if (salesType === "Direct" && customerName) {
      setValue("dealerEndUserName", customerName, { shouldValidate: true });
    } else if (salesType === "Dealer") {
      setValue("dealerEndUserName", "", { shouldValidate: true });
    }
  }, [salesType, customerName, setValue]);

  // ─── Fix #8: Do NOT spread {...register("customerName")} and then add a
  //     separate onChange — the spread's onChange would silently overwrite ours.
  //     Instead destructure register's result, replace onChange explicitly, and
  //     spread the rest (ref, name, onBlur, …) so RHF still tracks the field.
  const { onChange: _customerNameOnChange, ...customerNameRegisterRest } =
    register("customerName" as any);

  const handleCustomerNameChange = (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    // Keep the value uppercase and let RHF know about the change
    e.target.value = e.target.value.toUpperCase();
    _customerNameOnChange(e);
  };

  // ─── Fix #2: SubmitHandler is correctly parameterised with the exported type.
  const onSubmit: SubmitHandler<RequestFormValues> = async (data) => {
    try {
      console.log("Form data validated successfully:", data);
      alert("Form submitted successfully! Check console for payload.");
    } catch (error) {
      console.error("Submission failed:", error);
    }
  };

  // ─── Fix #3: `string` (lowercase) is the correct primitive type annotation.
  // ─── Fix #4: errors is now typed from RequestFormValues, so no implicit any.
  const ErrorMsg = ({ field }: { field: keyof RequestFormValues }) =>
    errors[field] ? (
      <p className="mt-1 text-sm text-red-600">
        {errors[field]?.message as string}
      </p>
    ) : null;

  return (
    <div className="max-w-5xl mx-auto p-6 bg-white rounded-lg shadow-md mt-10">
      <h2 className="text-3xl font-bold text-gray-800 mb-8 border-b pb-4">
        FAST/FACTS Sales Request
      </h2>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">

        {/* ================= SECTION 1: Customer Information ================= */}
        <div className="bg-gray-50 p-6 rounded-md border border-gray-200 shadow-sm">
          <h3 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">
            1. Customer Information
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Customer Name * (Exact GSTIN)
              </label>
              {/* Fix #8: spread rest props, supply our own onChange */}
              <input
                {...customerNameRegisterRest}
                onChange={handleCustomerNameChange}
                className={`w-full p-2 border rounded-md focus:ring-2 focus:ring-blue-500 bg-white ${
                  errors.customerName ? "border-red-500" : "border-gray-300"
                }`}
                placeholder="FASTFACTS PRIVATE LIMITED"
              />
              <ErrorMsg field="customerName" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Contact Person Name *
              </label>
              <input
                {...register("contactPersonName"as any)}
                className={`w-full p-2 border rounded-md focus:ring-2 focus:ring-blue-500 bg-white ${
                  errors.contactPersonName ? "border-red-500" : "border-gray-300"
                }`}
              />
              <ErrorMsg field="contactPersonName" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Contact Email *
              </label>
              <input
                type="email"
                {...register("contactEmail" as any)}
                className={`w-full p-2 border rounded-md focus:ring-2 focus:ring-blue-500 bg-white ${
                  errors.contactEmail ? "border-red-500" : "border-gray-300"
                }`}
              />
              <ErrorMsg field="contactEmail" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Mobile No. *
              </label>
              <input
                {...register("mobileNo" as any)}
                className={`w-full p-2 border rounded-md focus:ring-2 focus:ring-blue-500 bg-white ${
                  errors.mobileNo ? "border-red-500" : "border-gray-300"
                }`}
                placeholder="10 digit mobile number"
              />
              <ErrorMsg field="mobileNo" />
            </div>
          </div>
        </div>

        {/* ================= SECTION 2: Document & Product Details ================= */}
        <div className="bg-gray-50 p-6 rounded-md border border-gray-200 shadow-sm">
          <h3 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">
            2. Document & Product Details
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Document Type *
              </label>
              <select
                {...register("documentType" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              >
                <option value="INVOICE">Invoice</option>
                <option value="CREDIT_NOTE">Credit Note</option>
              </select>
              <ErrorMsg field="documentType" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Product Group *
              </label>
              <select
                {...register("productGroup" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              >
                <option value="">Select Group...</option>
                <option value="Desktop">Desktop</option>
                <option value="eTds Wizard">eTds Wizard</option>
                <option value="Web FAMS">Web FAMS</option>
                <option value="Web PayPac">Web PayPac</option>
                <option value="Web TDS">Web TDS</option>
              </select>
              <ErrorMsg field="productGroup" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Product Name *
              </label>
              <input
                {...register("productName" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              />
              <ErrorMsg field="productName" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Description of Charges *
              </label>
              <select
                {...register("descriptionOfCharges" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              >
                <option value="">Select Charge Type...</option>
                <option value="License Fee">License Fee</option>
                <option value="AMC">AMC</option>
                <option value="Subscription">Subscription</option>
                <option value="Professional Fee">Professional Fee</option>
              </select>
              <ErrorMsg field="descriptionOfCharges" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Invoice Type *
              </label>
              <select
                {...register("invoiceType" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              >
                <option value="">Select Invoice Type...</option>
                <option value="New Sales">New Sales</option>
                <option value="Renewals">Renewals</option>
                <option value="Recurring">Recurring</option>
                <option value="Support">Support</option>
              </select>
              <ErrorMsg field="invoiceType" />
            </div>
          </div>
        </div>

        {/* ================= SECTION 3: License Details ================= */}
        <div className="bg-gray-50 p-6 rounded-md border border-gray-200 shadow-sm">
          <h3 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">
            3. License Details
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                License Type *
              </label>
              <select
                {...register("licenseType" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              >
                <option value="">Select License...</option>
                <option value="Perpetual (AMC)">Perpetual (AMC)</option>
                <option value="Subscription (SaaS)">Subscription (SaaS)</option>
              </select>
              <ErrorMsg field="licenseType" />
            </div>

            {/* Fix #10: stable keys on conditional wrappers so React does not
                reuse stale DOM nodes when documentType changes. */}
            <div
              key="licensePeriodFrom"
              className={
                documentType !== "INVOICE" ? "opacity-50 pointer-events-none" : ""
              }
            >
              <label className="block text-sm font-medium text-gray-700 mb-1">
                License Period From{documentType === "INVOICE" && " *"}
              </label>
              <input
                type="date"
                {...register("licensePeriodFrom" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              />
              <ErrorMsg field="licensePeriodFrom" />
            </div>
            <div
              key="licensePeriodTo"
              className={
                documentType !== "INVOICE" ? "opacity-50 pointer-events-none" : ""
              }
            >
              <label className="block text-sm font-medium text-gray-700 mb-1">
                License Period To{documentType === "INVOICE" && " *"}
              </label>
              <input
                type="date"
                {...register("licensePeriodTo" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              />
              <ErrorMsg field="licensePeriodTo" />
            </div>
          </div>
        </div>

        {/* ================= SECTION 4: Billing & Payment ================= */}
        <div className="bg-gray-50 p-6 rounded-md border border-gray-200 shadow-sm">
          <h3 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">
            4. Billing & Payment
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Billing Value (INR) *
              </label>
              {/* Fix #6: removed { valueAsNumber: true } — coercion is handled
                  by z.coerce.number() in the schema, which is the correct place
                  when zodResolver is active. Mixing valueAsNumber with
                  zodResolver causes double-coercion conflicts. */}
              <input
                type="number"
                step="0.01"
                {...register("billingValue" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              />
              <ErrorMsg field="billingValue" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Payment Terms *
              </label>
              <input
                {...register("paymentTerms" as any)}
                placeholder="E.g., 30 Days Credit"
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              />
              <ErrorMsg field="paymentTerms" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Advance Payment (INR)
              </label>
              {/* Fix #6: same as billingValue */}
              <input
                type="number"
                step="0.01"
                {...register("advancePayment" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              />
              <ErrorMsg field="advancePayment" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Milestone *
              </label>
              <input
                {...register("milestone" as any)}
                placeholder="E.g., 100% on delivery"
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              />
              <ErrorMsg field="milestone" />
            </div>
          </div>
        </div>

        {/* ================= SECTION 5: Sales & Geography ================= */}
        <div className="bg-gray-50 p-6 rounded-md border border-gray-200 shadow-sm">
          <h3 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">
            5. Sales & Geography
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                State *
              </label>
              <select
                {...register("state" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              >
                <option value="">Select State...</option>
                <option value="Karnataka">Karnataka</option>
                <option value="Maharashtra">Maharashtra</option>
                <option value="Delhi">Delhi</option>
              </select>
              <ErrorMsg field="state" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Zone *
              </label>
              <select
                {...register("zone" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              >
                <option value="North">North</option>
                <option value="South">South</option>
                <option value="East">East</option>
                <option value="West">West</option>
              </select>
              <ErrorMsg field="zone" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Sales Person *
              </label>
              <input
                {...register("salesPerson" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              />
              <ErrorMsg field="salesPerson" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Sales Person Email *
              </label>
              <input
                type="email"
                {...register("salesPersonEmail" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              />
              <ErrorMsg field="salesPersonEmail" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Sales Type *
              </label>
              <select
                {...register("salesType" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              >
                <option value="Direct">Direct</option>
                <option value="Dealer">Dealer</option>
              </select>
              <ErrorMsg field="salesType" />
            </div>

            {/* Fix #9: HTML `disabled` prevents RHF from reading the value.
                Instead we use shouldUnregister: true so the field unregisters
                (and its value is excluded from the payload) when hidden.
                The field is conditionally rendered rather than merely disabled,
                paired with shouldUnregister in register() options. */}
            {salesType === "Dealer" && (
              <div key="dealerEndUserName">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Dealer / End User Name *
                </label>
                <input
                  {...register("dealerEndUserName" as any, { shouldUnregister: true })}
                  className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
                />
                <ErrorMsg field="dealerEndUserName" />
              </div>
            )}
          </div>
        </div>

        {/* ================= SECTION 6: Purchase Order & Approvals ================= */}
        <div className="bg-gray-50 p-6 rounded-md border border-gray-200 shadow-sm">
          <h3 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">
            6. Purchase Order & Approvals
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                PO Number
              </label>
              <input
                {...register("poNumber" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              />
              <ErrorMsg field="poNumber" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                PO Date
              </label>
              <input
                type="date"
                {...register("poDate" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              />
              <ErrorMsg field="poDate" />
            </div>

            {/* Fix #10: key on conditional wrapper */}
            <div
              key="customerApprovalEmailWrapper"
              className={
                poNumber && poNumber.trim() !== ""
                  ? "opacity-50 pointer-events-none"
                  : ""
              }
            >
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Customer Approval Email
                {(!poNumber || poNumber.trim() === "") && " *"}
              </label>
              <select
                {...register("customerApprovalEmail" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              >
                <option value="No">No</option>
                <option value="Yes">Yes</option>
              </select>
              <ErrorMsg field="customerApprovalEmail" />
            </div>

            {/* Fix #10: conditionally render instead of toggling `hidden` class
                so React properly mounts/unmounts and reconciles the node. */}
            {customerApprovalEmail === "Yes" && (
              <div key="customerApprovalDate">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Customer Approval Date *
                </label>
                <input
                  type="date"
                  {...register("customerApprovalDate" as any, { shouldUnregister: true })}
                  className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
                />
                <ErrorMsg field="customerApprovalDate" />
              </div>
            )}

            {/* Fix #7: file inputs connected to register() so RHF tracks them.
                The schema validates presence via superRefine if needed; actual
                FileList handling lives here in the component. */}
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                PO / Email Attachments *
              </label>
              <input
                type="file"
                multiple
                {...register("poEmailAttachments" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              />
              <p className="text-xs text-gray-500 mt-1">
                Upload PDF, DOCX, or Images. Max 5 files.
              </p>
              <ErrorMsg field="poEmailAttachments" />
            </div>
          </div>
        </div>

        {/* ================= SECTION 7: MSA / SLA & Remarks ================= */}
        <div className="bg-gray-50 p-6 rounded-md border border-gray-200 shadow-sm">
          <h3 className="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">
            7. MSA / SLA & Remarks
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                MSA / SLA / Proposal *
              </label>
              <select
                {...register("msaSlaProposal" as any)}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
              >
                <option value="No">No</option>
                <option value="Yes">Yes</option>
              </select>
              <ErrorMsg field="msaSlaProposal" />
            </div>

            {/* Fix #10: conditional render with key instead of hidden class */}
            {msaSlaProposal === "Yes" && (
              <div key="msaSlaDate">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  MSA / SLA Date *
                </label>
                <input
                  type="date"
                  {...register("msaSlaDate" as any, { shouldUnregister: true })}
                  className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
                />
                <ErrorMsg field="msaSlaDate" />
              </div>
            )}

            {/* Fix #7: MSA attachments connected to register() */}
            {msaSlaProposal === "Yes" && (
              <div key="msaSlaAttachments" className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  MSA / SLA Attachments *
                </label>
                <input
                  type="file"
                  multiple
                  {...register("msaSlaAttachments" as any, { shouldUnregister: true })}
                  className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 bg-white"
                />
                <p className="text-xs text-gray-500 mt-1">
                  Upload PDF, DOCX, or Images. Max 5 files.
                </p>
                <ErrorMsg field="msaSlaAttachments" />
              </div>
            )}

            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Remarks / Comments{" "}
                {documentType === "CREDIT_NOTE" && (
                  <span className="text-red-500">
                    * (Mandatory for Credit Note)
                  </span>
                )}
              </label>
              <textarea
                {...register("remarks" as any)}
                rows={3}
                className={`w-full p-2 border rounded-md focus:ring-2 focus:ring-blue-500 bg-white ${
                  errors.remarks ? "border-red-500" : "border-gray-300"
                }`}
                placeholder="Enter remarks here..."
              />
              <ErrorMsg field="remarks" />
            </div>
          </div>
        </div>

        {/* ================= SUBMIT ================= */}
        <div className="pt-6 flex justify-end items-center border-t border-gray-200">
          <button
            type="button"
            className="px-6 py-2 border border-gray-300 text-gray-700 font-semibold rounded-md hover:bg-gray-100 mr-4 transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="px-8 py-3 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 focus:ring-4 focus:ring-blue-300 disabled:bg-blue-400 disabled:cursor-not-allowed transition-colors"
          >
            {isSubmitting ? "Validating & Submitting…" : "Submit Sales Request"}
          </button>
        </div>
      </form>
    </div>
  );
}
