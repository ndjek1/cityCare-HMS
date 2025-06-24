package backingbeans;

import constants.PaymentStatus;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import models.Patient;
import models.ServiceItem;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Named("billBean")
@SessionScoped
public class BillBean implements Serializable {
    private Patient patient;
    private long appointmentId;
    private List<ServiceItem> serviceItems;
    private double totalAmount;
    private PaymentStatus paymentStatus;
    private LocalDateTime billDate;

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public List<ServiceItem> getServiceItems() {
        return serviceItems;
    }

    public void setServiceItems(List<ServiceItem> serviceItems) {
        this.serviceItems = serviceItems;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDateTime billDate) {
        this.billDate = billDate;
    }
}
