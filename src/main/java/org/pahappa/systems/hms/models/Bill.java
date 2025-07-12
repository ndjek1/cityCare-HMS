package org.pahappa.systems.hms.models;

import jakarta.persistence.*;
import org.pahappa.systems.hms.constants.PaymentStatus; // Assuming you have this enum
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bills")
public class Bill {
    // ... (billId, patient, appointmentId, billDate, paymentStatus as before) ...
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Long billId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    @OneToOne(fetch = FetchType.LAZY) // Or EAGER if you always need appointment details with the bill
    @JoinColumn(name = "appointment_id", referencedColumnName = "appointmentId", unique = true, nullable = false)
    private Appointment appointment;

    @Column(name = "bill_date")
    private LocalDateTime billDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @ElementCollection(fetch = FetchType.EAGER) // Eager fetch items with the bill
    @CollectionTable(name = "bill_items", joinColumns = @JoinColumn(name = "bill_id"))
    private List<BillItem> items = new ArrayList<>(); // Renamed from 'org.pahappa.systems.hms.navigation.services'

    @Column(name = "total_amount")
    private double totalAmount; // Denormalized for easy access, calculated on generation

    public Bill() {
        this.billDate = LocalDateTime.now();
    }

    public Bill(Patient patient, Appointment appointment) {
        this();
        this.patient = patient;
        this.appointment = appointment;
    }

    public void addItem(String serviceName, double cost, int quantity) {
        this.items.add(new BillItem(serviceName, cost, quantity));
        calculateTotalAmount();
    }


    public void calculateTotalAmount() {
        this.totalAmount = this.items.stream().mapToDouble(BillItem::getTotalCost).sum();
    }

    // Getters and Setters
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }
    public LocalDateTime getBillDate() { return billDate; }
    public void setBillDate(LocalDateTime billDate) { this.billDate = billDate; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public List<BillItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    // Convenience for UI if needed
    public Long getPatientId() { return patient != null ? patient.getPatientId() : null; }
}