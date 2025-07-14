package org.pahappa.systems.hms.services;

import org.pahappa.systems.hms.constants.PaymentMethod;
import org.pahappa.systems.hms.models.Appointment;
import org.pahappa.systems.hms.models.Bill;
import org.pahappa.systems.hms.models.BillItem;
import org.pahappa.systems.hms.models.Payment;


import java.util.List;
import java.util.Optional;

public interface BillingService {
    // Billing
    Optional<Bill> generateBill(Appointment appointment, List<BillItem> manuallyAddedItems);
    Optional<Bill> findBillById(Long billId);
    List<Bill> getUnpaidBillsForPatient(Long patientId);
    List<Bill> getAllUnpaidBills();

//    // Payment
    Optional<Payment> processPayment(Long billId, double amountPaid, PaymentMethod method);
    long getOpenBillsCount();
    List<Bill> getAllBills();
}