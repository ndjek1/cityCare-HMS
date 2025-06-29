package backingbeans.bill;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import models.Bill;
import models.Patient;
import models.Payment;
import constants.PaymentMethod; // Your enum

import services.impl.BillingServiceImpl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Named("paymentBean")
@ViewScoped
public class PaymentBean implements Serializable {

    private final BillingServiceImpl billingService;

    @Inject // To refresh bill lists after payment
    private BillBean billingBean; // If on admin page
    // OR @Inject private PatientBillingBean patientBillingBean; // If on patient page
    private Patient patient;
    private Long billIdToPay;
    private Bill billDetails;

    private double amountToPay; // BigDecimal
    private PaymentMethod selectedPaymentMethod;
    private String paymentReference;
    private String paymentNotes;

    private boolean dialogReady = false;

    public PaymentBean() {
        this.billingService = new BillingServiceImpl();
        System.out.println("PaymentBean CONSTRUCTOR - HashCode: " + System.identityHashCode(this));
    }

    @PostConstruct
    public void init() {
        System.out.println("PaymentBean @PostConstruct - HashCode: " + System.identityHashCode(this));
        resetPaymentForm();
    }

    public void resetPaymentForm() {
        this.billIdToPay = null;
        this.billDetails = null;
        this.amountToPay = 0.0;
        this.selectedPaymentMethod = null; // Or a default
        this.paymentReference = null;
        this.paymentNotes = null;
        this.dialogReady = false;
        System.out.println("PaymentBean: Form reset.");
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    // Called by a button (e.g., from BillingBean or an unpaid bills list)
    public void preparePaymentForBill(Long billId) {
        resetPaymentForm(); // Start fresh
        this.billIdToPay = billId;
        if (this.billIdToPay != null && billingService != null) {
            Optional<Bill> billOpt = billingService.findBillById(this.billIdToPay); // Ensure this method exists and initializes items
            if (billOpt.isPresent()) {
                this.billDetails = billOpt.get();
                this.patient = billOpt.get().getPatient();
                // Pre-fill amountToPay with remaining balance if possible
                // This requires knowing amount already paid. For simplicity, pre-fill with total.
                this.amountToPay = this.billDetails.getTotalAmount(); // Assumes Bill.getTotalAmount() is BigDecimal
                this.dialogReady = true;
                System.out.println("PaymentBean: Prepared payment for Bill ID: " + this.billIdToPay +
                        ", Amount Due: " + this.amountToPay);
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Bill (ID: " + billId + ") not found."));
                this.dialogReady = false;
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Cannot prepare payment. Bill ID or service missing."));
            this.dialogReady = false;
        }
    }

    public void submitPayment() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (!dialogReady || billDetails == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Payment form not ready or bill details missing."));
            return;
        }
        if (amountToPay <= 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Payment amount must be greater than zero."));
            return;
        }
        if (selectedPaymentMethod == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Please select a payment method."));
            return;
        }
        // Optional: Validate if amountToPay exceeds outstanding balance if you track it.

        System.out.println("PaymentBean: Submitting payment for Bill ID: " + billDetails.getBillId() +
                ", Amount: " + amountToPay + ", Method: " + selectedPaymentMethod);

        Optional<Payment> paymentOpt = billingService.processPayment(
                billDetails.getBillId(),
                amountToPay,
                selectedPaymentMethod
        );

        if (paymentOpt.isPresent()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Payment Successful",
                    "Payment of " + amountToPay + " processed for Bill ID: " + billDetails.getBillId()));
            resetPaymentForm(); // Clear form for next use

            // Refresh relevant bill lists
            if (billingBean != null) { // If called from admin page
                billingBean.loadAllUnpaidBills();
            }
            // if (patientBillingBean != null) { // If called from patient page
            //     patientBillingBean.loadMyUnpaidBills();
            // }

            // Consider hiding the dialog via oncomplete in XHTML
            // PF('paymentDialogWidget').hide();
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Payment Failed",
                    "Could not process payment. Please check details or contact support."));
        }
    }

    // Getters and Setters
    public Long getBillIdToPay() { return billIdToPay; }
    public Bill getBillDetails() { return billDetails; }
    public double getAmountToPay() { return amountToPay; }
    public void setAmountToPay(double amountToPay) { this.amountToPay = amountToPay; }
    public PaymentMethod getSelectedPaymentMethod() { return selectedPaymentMethod; }
    public void setSelectedPaymentMethod(PaymentMethod selectedPaymentMethod) { this.selectedPaymentMethod = selectedPaymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public String getPaymentNotes() { return paymentNotes; }
    public void setPaymentNotes(String paymentNotes) { this.paymentNotes = paymentNotes; }
    public boolean isDialogReady() { return dialogReady; }

    // For payment method dropdown
    public List<PaymentMethod> getAvailablePaymentMethods() {
        return Arrays.asList(PaymentMethod.values());
    }
}