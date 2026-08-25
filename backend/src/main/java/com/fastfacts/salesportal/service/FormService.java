package com.fastfacts.salesportal.service;

import com.fastfacts.salesportal.dto.form.CfoReviewRequest;
import com.fastfacts.salesportal.dto.form.FinanceCloseRequest;
import com.fastfacts.salesportal.dto.form.FormCreateRequest;
import com.fastfacts.salesportal.entity.Form;
import com.fastfacts.salesportal.entity.FormStatusHistory;
import com.fastfacts.salesportal.entity.User;
import com.fastfacts.salesportal.entity.enums.FormStatus;
import com.fastfacts.salesportal.repository.FormRepository;
import com.fastfacts.salesportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FormService {

    private final FormRepository formRepository;
    private final UserRepository userRepository;

    public FormService(FormRepository formRepository, UserRepository userRepository) {
        this.formRepository = formRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Form createForm(FormCreateRequest request, String userEmail) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Form form = new Form();
        mapToEntity(request, form);
        form.setCreatedBy(creator);
        form.setCurrentStatus(FormStatus.SUBMITTED); // Initialize status just to be safe, addStatusHistory handles it

        FormStatusHistory history = new FormStatusHistory();
        history.setChangedBy(creator);
        history.setPreviousStatus(null);
        history.setNewStatus(FormStatus.SUBMITTED);
        history.setComments("Initial Form Submission");

        form.addStatusHistory(history);
        return formRepository.save(form);
    }

    @Transactional
    public Form cfoReview(Long formId, CfoReviewRequest request, String userEmail) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        if (form.getCurrentStatus() != FormStatus.SUBMITTED && form.getCurrentStatus() != FormStatus.RESUBMITTED) {
            throw new RuntimeException("Form is not in a reviewable state.");
        }

        User reviewer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FormStatus newStatus;
        switch (request.getAction().toUpperCase()) {
            case "APPROVE":
                newStatus = FormStatus.APPROVED;
                break;
            case "REJECT":
                newStatus = FormStatus.REJECTED;
                if (request.getComments() == null || request.getComments().trim().isEmpty()) {
                    throw new RuntimeException("Comments are mandatory for rejection.");
                }
                break;
            case "NEED_MORE_INFO":
                newStatus = FormStatus.NEED_MORE_INFO;
                if (request.getComments() == null || request.getComments().trim().isEmpty()) {
                    throw new RuntimeException("Comments are mandatory when requesting more info.");
                }
                break;
            default:
                throw new RuntimeException("Invalid action.");
        }

        FormStatusHistory history = new FormStatusHistory();
        history.setChangedBy(reviewer);
        history.setPreviousStatus(form.getCurrentStatus());
        history.setNewStatus(newStatus);
        history.setComments(request.getComments());

        form.addStatusHistory(history);
        return formRepository.save(form);
    }

    @Transactional
    public Form resubmitForm(Long formId, FormCreateRequest request, String userEmail) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        if (form.getCurrentStatus() != FormStatus.NEED_MORE_INFO) {
            throw new RuntimeException("Form is not in a state that allows resubmission.");
        }

        User submitter = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!form.getCreatedBy().getId().equals(submitter.getId())) {
            throw new RuntimeException("Only the original creator can resubmit the form.");
        }

        mapToEntity(request, form); // Update fields with new data

        FormStatusHistory history = new FormStatusHistory();
        history.setChangedBy(submitter);
        history.setPreviousStatus(form.getCurrentStatus());
        history.setNewStatus(FormStatus.RESUBMITTED);
        history.setComments("Form Resubmitted");

        form.addStatusHistory(history);
        return formRepository.save(form);
    }

    @Transactional
    public Form financeClose(Long formId, FinanceCloseRequest request, String userEmail) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        if (form.getCurrentStatus() != FormStatus.APPROVED) {
            throw new RuntimeException("Form is not approved. Cannot close workflow.");
        }

        User financeOfficer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FormStatusHistory history = new FormStatusHistory();
        history.setChangedBy(financeOfficer);
        history.setPreviousStatus(form.getCurrentStatus());
        history.setNewStatus(FormStatus.CLOSED);
        history.setComments(request.getComments());

        form.addStatusHistory(history);
        return formRepository.save(form);
    }

    private void mapToEntity(FormCreateRequest request, Form form) {
        form.setCustomerName(request.getCustomerName());
        form.setContactPersonName(request.getContactPersonName());
        form.setContactEmail(request.getContactEmail());
        form.setMobileNo(request.getMobileNo());
        form.setDocumentType(request.getDocumentType());
        form.setProductGroup(request.getProductGroup());
        form.setProductName(request.getProductName());
        form.setDescriptionOfCharges(request.getDescriptionOfCharges());
        form.setInvoiceType(request.getInvoiceType());
        form.setLicensePeriodFrom(request.getLicensePeriodFrom());
        form.setLicensePeriodTo(request.getLicensePeriodTo());
        form.setLicenseType(request.getLicenseType());
        form.setBillingValue(request.getBillingValue());
        form.setPaymentTerms(request.getPaymentTerms());
        form.setAdvancePayment(request.getAdvancePayment());
        form.setMilestone(request.getMilestone());
        form.setState(request.getState());
        form.setSalesPerson(request.getSalesPerson());
        form.setSalesPersonEmail(request.getSalesPersonEmail());
        form.setZone(request.getZone());
        form.setSalesType(request.getSalesType());
        form.setDealerEndUserName(request.getDealerEndUserName());
        form.setPoNumber(request.getPoNumber());
        form.setPoDate(request.getPoDate());
        form.setCustomerApprovalEmail(request.getCustomerApprovalEmail());
        form.setCustomerApprovalDate(request.getCustomerApprovalDate());
        form.setMsaSlaProposal(request.getMsaSlaProposal());
        form.setMsaSlaDate(request.getMsaSlaDate());
        form.setRemarks(request.getRemarks());

        if (request.getPoEmailAttachments() != null) {
            form.setPoEmailAttachments(request.getPoEmailAttachments());
        }
        if (request.getMsaSlaAttachments() != null) {
            form.setMsaSlaAttachments(request.getMsaSlaAttachments());
        }
    }
}
