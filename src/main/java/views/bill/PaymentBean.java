package views.bill;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Bill;
import org.pahappa.systems.hms.models.Patient;
import org.pahappa.systems.hms.models.Payment;
import org.pahappa.systems.hms.constants.PaymentMethod; // Your enum

import org.pahappa.systems.hms.services.impl.BillingServiceImpl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Named("paymentBean")
@SessionScoped
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
        this.dialogReady  = false;
        System.out.println("PaymentBean @PostConstruct - HashCode: " + System.identityHashCode(this));
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

    // Called by the "Process Payment" button in the dataTable
    public void preparePaymentForBill(Bill billToPay) {
        // This method now receives the whole Bill object, which is cleaner.
        // Ensure the Bill object from the dataTable has its lazy associations initialized if needed.
        if (billToPay != null) {
            this.billDetails = billToPay;

            // Pre-fill amountToPay with remaining balance
            this.amountToPay = billToPay.getTotalAmount(); // Assuming Bill has an getAmountDue() method
            this.selectedPaymentMethod = null;
            this.paymentReference = null;
            this.paymentNotes = null;
            this.dialogReady = true; // Mark dialog as ready to be displayed
            System.out.println("PaymentBean: Prepared payment for Bill ID: " + billToPay.getBillId() + ", Amount Due: " + this.amountToPay);
        } else {
            this.dialogReady = false;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Selected bill is invalid."));
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

    public void setBillDetails(Bill billDetails) {
        this.billDetails = billDetails;
    }

    public double getAmountToPay() { return amountToPay; }
    public void setAmountToPay(double amountToPay) { this.amountToPay = amountToPay; }
    public PaymentMethod getSelectedPaymentMethod() { return selectedPaymentMethod; }
    public void setSelectedPaymentMethod(PaymentMethod selectedPaymentMethod) { this.selectedPaymentMethod = selectedPaymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public String getPaymentNotes() { return paymentNotes; }
    public void setPaymentNotes(String paymentNotes) { this.paymentNotes = paymentNotes; }
    public boolean isDialogReady() { return dialogReady; }

    public BillingServiceImpl getBillingService() {
        return billingService;
    }

    public BillBean getBillingBean() {
        return billingBean;
    }

    public void setBillingBean(BillBean billingBean) {
        this.billingBean = billingBean;
    }

    public void setBillIdToPay(Long billIdToPay) {
        this.billIdToPay = billIdToPay;
    }

    public void setDialogReady(boolean dialogReady) {
        this.dialogReady = dialogReady;
    }

    // For payment method dropdown
    public List<PaymentMethod> getAvailablePaymentMethods() {
        return Arrays.asList(PaymentMethod.values());
    }
}