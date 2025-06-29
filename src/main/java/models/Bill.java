package models;

import jakarta.persistence.*;
import constants.PaymentStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billId;
    @ManyToOne
    @JoinColumn(name = "patient_id_fk")
    private Patient patient;     // FK
    private Long appointmentId; // Optional FK
    @ElementCollection(fetch = FetchType.LAZY) // Eagerly fetching collections can lead to performance issues
    @CollectionTable(name = "bill_service_items", joinColumns = @JoinColumn(name = "bill_id"))
    private List<ServiceItem> services; // In DB, this would be a separate BillItems table
    private double totalAmount;
    private PaymentStatus paymentStatus;
    private LocalDateTime billDate;

    public Bill(Patient patientId, Long appointmentId) {
        this.patient = patientId;
        this.appointmentId = appointmentId;
        this.services = new ArrayList<>();
        this.totalAmount = 0.0;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.billDate = LocalDateTime.now();
    }

    public Bill() {

    }

    public void addService(String serviceName, double cost) {
        this.services.add(new ServiceItem(serviceName, cost));
        this.totalAmount += cost;
    }
    // Getters and setters... (same as before)
    public Long getBillId() { return billId; }
    public Patient getPatientId() { return patient; }
    public Long getAppointmentId() { return appointmentId; }
    public List<ServiceItem> getServices() { return services; }
    public double getTotalAmount() { return totalAmount; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getBillDate() { return billDate; }


    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String currencySymbol = "UGX"; // Or Shs. etc.
        int itemIndent = 4;
        int sectionIndent = 2;
        String lineSeparator = System.lineSeparator(); // Use system-dependent line separator

        StringBuilder sb = new StringBuilder();

        sb.append("=========================================").append(lineSeparator);
        sb.append("           CITYCARE GENERAL HOSPITAL       ").append(lineSeparator);
        sb.append("                  BILL INVOICE             ").append(lineSeparator);
        sb.append("=========================================").append(lineSeparator);

        sb.append(String.format("%-" + sectionIndent + "sBill ID         : %s%n", "", billId));
        if (patient != null && patient.getName() != null) {
            sb.append(String.format("%-" + sectionIndent + "sPatient Name    : %s (ID: %d)%n", "", patient.getName(), patient.getPatientId()));
        } else {
            sb.append(String.format("%-" + sectionIndent + "sPatient ID      : %d%n", "", patient.getPatientId()));
        }
        if (appointmentId != null) {
            sb.append(String.format("%-" + sectionIndent + "sAppointment ID  : %d%n", "", appointmentId));
        }
        sb.append(String.format("%-" + sectionIndent + "sDate Issued     : %s%n", "", billDate.format(dtf)));
        sb.append(String.format("%-" + sectionIndent + "sPayment Status  : %s%n", "", paymentStatus));
        sb.append("-----------------------------------------").append(lineSeparator);

        sb.append(String.format("%-" + sectionIndent + "sServices Rendered:%n", ""));
        if (services == null || services.isEmpty()) {
            sb.append(String.format("%-" + itemIndent + "sNo services listed.%n", ""));
        } else {
            for (ServiceItem item : services) {
                // Adjust padding for alignment based on expected name length
                sb.append(String.format("%-" + itemIndent + "s- %-25s : %s %,.2f%n",
                        "",
                        item.getName(),
                        currencySymbol,
                        item.getCost()));
            }
        }
        sb.append("-----------------------------------------").append(lineSeparator);

        sb.append(String.format("%-" + sectionIndent + "sTOTAL AMOUNT    : %s %,.2f%n",
                "",
                currencySymbol,
                totalAmount));
        sb.append("=========================================").append(lineSeparator);
        sb.append(String.format("%-" + sectionIndent + "sThank you for choosing CityCare!%n", ""));
        sb.append("=========================================").append(lineSeparator);

        return sb.toString();
    }
}