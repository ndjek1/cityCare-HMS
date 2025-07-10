package views.bill;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Bill;
import org.pahappa.systems.hms.models.Payment;
import org.pahappa.systems.hms.constants.PaymentMethod;
import org.pahappa.systems.hms.services.BillingService; // Use the interface
import org.pahappa.systems.hms.services.impl.BillingServiceImpl;

import java.io.Serializable;
import java.math.BigDecimal; // Use BigDecimal for currency
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Named("billBean")
@ViewScoped // Use ViewScoped for page-specific state
public class BillBean implements Serializable {

    private final BillingServiceImpl billingService; // CORRECT: Use DI for the service

    // === Properties for displaying the list of bills ===
    private List<Bill> allUnpaidBills;

    // === Properties for the payment dialog ===
    private Bill billToPay; // The bill currently loaded in the dialog
    private double amountToPay; // BigDecimal for precision
    private PaymentMethod selectedPaymentMethod;
    private String paymentReference;
    private String paymentNotes;
    private boolean dialogReady = false;

    public BillBean() {
        // Constructor should be empty. DI happens after.
        this.billingService = new BillingServiceImpl();
        System.out.println("BillBean CONSTRUCTOR called.");
    }

    @PostConstruct
    public void init() {
        System.out.println("BillBean @PostConstruct init() called.");
        // Load the initial data for the page
        loadAllUnpaidBills();
    }

    // --- List Loading Logic ---
    public void loadAllUnpaidBills() {
        System.out.println("Loading all unpaid bills...");
        if (billingService != null) {
            this.allUnpaidBills = billingService.getAllUnpaidBills();
        }
        if (this.allUnpaidBills == null) { // Defensive null check
            this.allUnpaidBills = new ArrayList<>();
        }
        this.dialogReady = true;
        System.out.println("Loaded " + this.allUnpaidBills.size() + " unpaid bills.");
    }

    // --- Dialog Management Logic ---
    public void preparePaymentDialog(Bill bill) {
        if (bill == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid bill selected."));
            this.dialogReady = false;
            return;
        }
        this.billToPay = bill;
        this.amountToPay = bill.getTotalAmount(); // Assuming Bill has getAmountDue()
        this.selectedPaymentMethod = null;
        this.paymentReference = null;
        this.paymentNotes = null;
        this.dialogReady = true; // Mark dialog as ready
        System.out.println("Prepared payment dialog for Bill ID: " + bill.getBillId());
    }

    public void submitPayment() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (!dialogReady || billToPay == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Payment form not ready or bill details missing."));
            return;
        }
        if ( amountToPay <= 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Payment amount must be greater than zero."));
            return;
        }
        if (selectedPaymentMethod == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Please select a payment method."));
            return;
        }

        System.out.println("Submitting payment for Bill ID: " + billToPay.getBillId());

        Optional<Payment> paymentOpt = billingService.processPayment(
                billToPay.getBillId(),
               amountToPay,
                selectedPaymentMethod
        );

        if (paymentOpt.isPresent()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Payment Successful",
                    "Payment processed for Bill ID: " + billToPay.getBillId()));
            this.dialogReady = false; // "Close" the dialog logically
            loadAllUnpaidBills(); // Refresh the main list to show updated status
            // The oncomplete script in XHTML will handle hiding the visual dialog
        } else {
            // Error message is added to a component inside the dialog
            context.addMessage("paymentDialogForm:paymentMessages", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Payment Failed",
                    "Could not process payment. Please check details or contact support."));
        }
    }

    // --- Getters and Setters ---
    public List<Bill> getAllUnpaidBills() {
        return allUnpaidBills;
    }

    public Bill getBillToPay() {
        return billToPay;
    }

    public double getAmountToPay() {
        return amountToPay;
    }

    public void setAmountToPay(double amountToPay) {
        this.amountToPay = amountToPay;
    }

    public PaymentMethod getSelectedPaymentMethod() {
        return selectedPaymentMethod;
    }

    public void setSelectedPaymentMethod(PaymentMethod selectedPaymentMethod) {
        this.selectedPaymentMethod = selectedPaymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getPaymentNotes() {
        return paymentNotes;
    }

    public void setPaymentNotes(String paymentNotes) {
        this.paymentNotes = paymentNotes;
    }

    public boolean isDialogReady() {
        return dialogReady;
    }

    public List<PaymentMethod> getAvailablePaymentMethods() {
        return Arrays.asList(PaymentMethod.values());
    }

    public void setBillToPay(Bill billToPay) {
        this.billToPay = billToPay;
    }

    public void setDialogReady(boolean dialogReady) {
        this.dialogReady = dialogReady;
    }
}