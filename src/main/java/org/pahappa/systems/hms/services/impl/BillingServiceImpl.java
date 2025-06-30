package org.pahappa.systems.hms.services.impl;

import org.pahappa.systems.hms.constants.PaymentMethod;
import org.pahappa.systems.hms.constants.PaymentStatus;
import org.pahappa.systems.hms.dao.impl.BillDaoImpl;
import org.pahappa.systems.hms.dao.impl.PaymentDaoImpl;
import jakarta.inject.Inject;
import org.pahappa.systems.hms.models.Appointment;
import org.pahappa.systems.hms.models.Bill;
import org.pahappa.systems.hms.models.BillItem;
import org.pahappa.systems.hms.models.Payment;
import org.pahappa.systems.hms.services.BillingService;

import java.util.List;
import java.util.Optional;

public class BillingServiceImpl implements BillingService {

    private  final BillDaoImpl billDao;

    private final PaymentDaoImpl paymentDao;
    @Inject
    private ServiceCatalogServiceImpl serviceCatalogServiceImpl; // Example of service injecting service

    public BillingServiceImpl() {
        this.billDao = new BillDaoImpl();
        this.paymentDao = new PaymentDaoImpl();
    }


    @Override
    public Optional<Bill> generateBill(Appointment appointment, List<BillItem> manuallyAddedItems) {

        try {


            if(appointment != null) {
                Optional<Bill> billOpt = billDao.findByAppointmentId(appointment.getAppointmentId());
                if (billOpt.isPresent()) {
                    return Optional.empty();
                }
                Bill bill = new Bill(appointment.getPatient(), appointment);
                bill.addItem("Doctor Consultation", 150.00, 1);
                if (manuallyAddedItems != null) {
                    manuallyAddedItems.forEach(item -> bill.addItem(item.getServiceName(), item.getCostAtTimeOfBilling(), item.getQuantity()));
                }
                billDao.save(bill);
                return Optional.of(bill);
            }


        } catch (Exception e) {

            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.empty();
    }

    // ... Implement other billing methods like processPayment, getAllUnpaidBills, etc.
@Override
    public List<Bill> getAllUnpaidBills() {

        List<Bill> allUnpaidBills;

        System.out.println("SERVICE: Fetching all unpaid or partially paid bills.");
       allUnpaidBills = billDao.findAllUnpaid();

        return allUnpaidBills;
    }
@Override
    public Optional<Bill> findBillById(Long billId) {

        try {


            Optional<Bill> billOpt = billDao.findById(billId);

                Bill bill = billOpt.orElse(null);
            return Optional.ofNullable(bill);
        }catch (Exception e) {

            e.printStackTrace(); // Log error
        }
        return Optional.empty();
    }



    @Override
    public  List<Bill> getUnpaidBillsForPatient(Long patientId){
       return billDao.findByPatientId(patientId);

    }

    @Override
    public Optional<Payment> processPayment(Long billId, double amountPaid, PaymentMethod method) {
        if (billId == null || amountPaid <= 0) {
            System.err.println("Invalid billId or amountPaid. billId=" + billId + ", amountPaid=" + amountPaid);
            return Optional.empty();
        }

        try {
            Optional<Bill> billOpt = billDao.findById(billId);

            if (billOpt.isPresent()) {
                Bill bill = billOpt.get();

                if (bill.getPaymentStatus() == PaymentStatus.PAID) {
                    System.out.println("Bill " + billId + " is already fully paid.");
                    return Optional.empty();
                }

                Payment payment = new Payment(bill, amountPaid, method);


                paymentDao.save(payment);

                if (amountPaid >= bill.getTotalAmount()) {
                    bill.setPaymentStatus(PaymentStatus.PAID);
                } else {
                    bill.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
                }

                billDao.update(bill);
                return Optional.of(payment);
            } else {
                System.err.println("No bill found for ID: " + billId);
            }
        } catch (Exception e) {
            e.printStackTrace(); // You can log this more cleanly in real apps
        }

        return Optional.empty();
    }





}