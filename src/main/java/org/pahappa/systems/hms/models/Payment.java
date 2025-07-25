package org.pahappa.systems.hms.models;

import jakarta.persistence.*;
import org.pahappa.systems.hms.constants.PaymentMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id_fk")
    private Bill billId; // FK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id_fk")
    private Prescription prescription;
    @Column(name = "amount_paid")
    private double amountPaid;
    private PaymentMethod method;
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    public Payment(Bill billId, double amountPaid, PaymentMethod method) {
        this.billId = billId;
        this.amountPaid = amountPaid;
        this.method = method;
        this.paymentDate = LocalDateTime.now();
    }
    public  Payment(Prescription prescription, double amountPaid, PaymentMethod method) {
        this.prescription = prescription;
        this.amountPaid = amountPaid;
        this.method = method;
        this.paymentDate = LocalDateTime.now();
    }

    public Payment() {

    }

    // Getters... (same as before)
    public Long getPaymentId() { return paymentId; }
    public Bill getBillId() { return billId; }
    public double getAmountPaid() { return amountPaid; }
    public PaymentMethod getMethod() { return method; }
    public LocalDateTime getPaymentDate() { return paymentDate; }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Added seconds for precision
        String currencySymbol = "UGX"; // Or get from a config or locale
        String lineSeparator = System.lineSeparator();
        int labelWidth = 18; // Consistent label width

        StringBuilder sb = new StringBuilder();

        sb.append("--------------------------------------").append(lineSeparator);
        sb.append("            PAYMENT DETAILS             ").append(lineSeparator);
        sb.append("--------------------------------------").append(lineSeparator);

        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Payment ID", paymentId != null ? paymentId : "N/A"));
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Associated Bill ID", billId != null ? billId : "N/A"));
        sb.append(String.format("%-" + labelWidth + "s: %s %,.2f%n", "Amount Paid",
                currencySymbol,
                amountPaid)); // Using %,.2f for comma separation and 2 decimal places
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Payment Method", method != null ? method : "N/A"));
        sb.append(String.format("%-" + labelWidth + "s: %s%n", "Payment Date",
                paymentDate != null ? paymentDate.format(dtf) : "N/A"));

        sb.append("--------------------------------------").append(lineSeparator);

        return sb.toString();
    }
}
