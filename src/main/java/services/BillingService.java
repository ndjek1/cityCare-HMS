package services;

import constants.PaymentMethod;
import models.*;
import constants.ServiceCategory;

import java.math.BigDecimal;
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
//
//    // Service Catalog
//    Optional<ServiceCatalogItem> addServiceToCatalog(String code, String name, double cost, ServiceCategory category, String description);
//    List<ServiceCatalogItem> getFullServiceCatalog();
//    List<ServiceCatalogItem> getActiveServiceCatalogItems();
//
//    Optional<Payment> processPayment(Long billId, double amountPaid, PaymentMethod method);
}