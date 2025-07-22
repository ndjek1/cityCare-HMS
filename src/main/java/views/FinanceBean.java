package views;


import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Bill;
import org.pahappa.systems.hms.models.Payment;
import org.pahappa.systems.hms.constants.PaymentMethod;
import org.pahappa.systems.hms.models.Prescription;
import org.pahappa.systems.hms.services.impl.PrescriptionServiceImpl;

import java.io.Serializable;
import java.math.BigDecimal; // Use BigDecimal for currency
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Named("financeBean")
@ViewScoped // Use ViewScoped for page-specific state
public class FinanceBean implements Serializable {

    private final PrescriptionServiceImpl prescriptionService;
    private List<Prescription> allUnpaidPrescriptions;
    private List<Prescription> filteredPrescriptions;

    // === Properties for the payment dialog ===
    private Prescription prescriptionToPay; // The bill currently loaded in the dialog
    private double amountToPay; // BigDecimal for precision
    private PaymentMethod selectedPaymentMethod;
    private String paymentReference;
    private String paymentNotes;
    private boolean dialogReady = false;

    public FinanceBean() {

        this.prescriptionService = new PrescriptionServiceImpl();
        System.out.println("BillBean CONSTRUCTOR called.");
    }

    @PostConstruct
    public void init() {
        System.out.println("BillBean @PostConstruct init() called.");
        // Load the initial data for the page
        loadAllUnpaidPrescriptions();
    }


    // --- List Loading Logic ---
    public void loadAllUnpaidPrescriptions() {
        System.out.println("Loading all unpaid bills...");
        if (prescriptionService != null) {
            this.allUnpaidPrescriptions = prescriptionService.findAllUnpaid();
        }
        if (this.allUnpaidPrescriptions == null) { // Defensive null check
            this.allUnpaidPrescriptions = new ArrayList<>();
        }
        this.dialogReady = true;
        this.filteredPrescriptions = this.allUnpaidPrescriptions;
        System.out.println("Loaded " + this.allUnpaidPrescriptions.size() + " unpaid bills.");
    }

    // ✅ Global Filter Function
    public boolean globalFilterFunction(Object value, Object filter, java.util.Locale locale) {

        Prescription prescription = (Prescription) value;
        // Case 1: Text filter from search box (String)
        if (filter instanceof String filterText) {
            String lowerFilter = filterText.toLowerCase().trim();
            if (lowerFilter.isBlank()) return true;

            return (prescription.getPatient().getName() != null && prescription.getPatient().getName().toLowerCase().contains(filterText)) ||
                    (prescription.getTotalCost() > 0 && String.valueOf(prescription.getTotalCost()).toLowerCase().contains(filterText)) ||
                    (prescription.getPrescriptionDate() != null && prescription.getPrescriptionDate().toString().contains(lowerFilter));
        }
        return true;
    }

    // --- Dialog Management Logic ---
    public void preparePaymentDialog(Prescription prescription) {
        if (prescription == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid prescription selected."));
            this.dialogReady = false;
            return;
        }
        this.prescriptionToPay = prescription;
        this.amountToPay = prescription.getTotalCost(); // Assuming Bill has getAmountDue()
        this.selectedPaymentMethod = null;
        this.paymentReference = null;
        this.paymentNotes = null;
        this.dialogReady = true; // Mark dialog as ready
        System.out.println("Prepared payment dialog for Bill ID: " + prescription.getPrescriptionId());
    }

    public void submitPayment() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (!dialogReady || prescriptionToPay == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Payment form not ready or prescription details missing."));
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

        System.out.println("Submitting payment for Bill ID: " + prescriptionToPay.getPrescriptionId());

        Optional<Payment> paymentOpt = prescriptionService.processPaymentForPrescription(
                prescriptionToPay.getPrescriptionId(),
                amountToPay,
                selectedPaymentMethod
        );

        if (paymentOpt.isPresent()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Payment Successful",
                    "Payment processed for Bill ID: " + prescriptionToPay.getPrescriptionId()));
            this.dialogReady = false; // "Close" the dialog logically
            loadAllUnpaidPrescriptions(); // Refresh the main list to show updated status

        } else {
            // Error message is added to a component inside the dialog
            context.addMessage("paymentDialogForm:paymentMessages", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Payment Failed",
                    "Could not process payment. Please check details or contact support."));
        }
    }


    public List<Prescription> getFilteredPrescriptions() {
        return filteredPrescriptions;
    }

    public void setFilteredPrescriptions(List<Prescription> filteredPrescriptions) {
        this.filteredPrescriptions = filteredPrescriptions;
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

    public Prescription getPrescriptionToPay() {
        return prescriptionToPay;
    }

    public void setPrescriptionToPay(Prescription prescriptionToPay) {
        this.prescriptionToPay = prescriptionToPay;
    }

    public List<Prescription> getAllUnpaidPrescriptions() {
        return allUnpaidPrescriptions;
    }

    public void setAllUnpaidPrescriptions(List<Prescription> allUnpaidPrescriptions) {
        this.allUnpaidPrescriptions = allUnpaidPrescriptions;
    }

    public void setDialogReady(boolean dialogReady) {
        this.dialogReady = dialogReady;
    }
}
