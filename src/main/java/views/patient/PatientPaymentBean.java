package views.patient;


import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Patient;
import org.pahappa.systems.hms.models.Payment;
import org.pahappa.systems.hms.services.impl.PatientServiceImpl;
import views.UserAccountBean;   // To get the logged-in patient info

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Named("patientFinancialsBean")
@ViewScoped
public class PatientPaymentBean implements Serializable {

    private final PatientServiceImpl patientService; // Or BillingService, wherever you placed the method

    @Inject
    private UserAccountBean userAccountBean;

    private List<Payment> paymentHistory;
    private Patient currentPatient;
    private LocalDate paymentDateFilter;
    private Payment selectedPayment;

    public PatientPaymentBean() {
        this.patientService = new PatientServiceImpl();
    }

    @PostConstruct
    public void init() {
        System.out.println("PatientFinancialsBean: Initializing...");
        loadPaymentHistory();
    }

    public void loadPaymentHistory() {
        this.paymentHistory = new ArrayList<>(); // Initialize to avoid NPE
        if (userAccountBean.isLoggedIn() && userAccountBean.isPatient()) {
            Object patientDetails = userAccountBean.getCurrentUserDetails();
            if (patientDetails instanceof Patient) {
                this.currentPatient = (Patient) patientDetails;
                if (this.currentPatient.getPatientId() != null && patientService != null) {
                    // Call the new service method
                    this.paymentHistory = patientService.findPaymentsForPatient(this.currentPatient.getPatientId());
                }
            }
        }
        if (this.paymentHistory == null) { // Defensive check
            this.paymentHistory = new ArrayList<>();
        }
        System.out.println("PatientFinancialsBean: Loaded " + this.paymentHistory.size() + " payment records.");
    }

    public boolean filterByDate(Object value, Object filter, Locale locale) {
        if (filter == null || value == null) return true;
        if (!(value instanceof LocalDateTime) || !(filter instanceof LocalDate)) return true;

        LocalDateTime paymentDateTime = (LocalDateTime) value;
        LocalDate filterDate = (LocalDate) filter;

        return paymentDateTime.toLocalDate().isEqual(filterDate);
    }

    public void prepareReceipt(Payment payment) {
        this.selectedPayment = payment;
    }


    public Payment getSelectedPayment() {
        return selectedPayment;
    }

    public void setSelectedPayment(Payment selectedPayment) {
        this.selectedPayment = selectedPayment;
    }

    // Getters for the JSF page
    public List<Payment> getPaymentHistory() {
        return paymentHistory;
    }

    public Patient getCurrentPatient() {
        return currentPatient;
    }

    public LocalDate getPaymentDateFilter() {
        return paymentDateFilter;
    }

    public void setPaymentDateFilter(LocalDate paymentDateFilter) {
        this.paymentDateFilter = paymentDateFilter;
    }
}