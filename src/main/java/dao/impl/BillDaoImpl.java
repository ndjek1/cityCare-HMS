package dao.impl;


import constants.PaymentStatus;
import dao.BillDao;
import jakarta.enterprise.context.ApplicationScoped;
import models.Bill;
import models.Staff;
import org.hibernate.Hibernate;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BillDaoImpl extends AbstractDao<Bill, Long> implements BillDao {

    public BillDaoImpl() {
        super(Bill.class);
    }

    @Override
    public List<Bill> findByPatientId(Long patientId) {
        return execute(session -> {
            if (patientId == null) return Collections.emptyList();
            Query<Bill> query = session.createQuery(
                    "SELECT b FROM Bill b WHERE b.patient.patientId = :patientId ORDER BY b.billDate DESC", Bill.class);
            query.setParameter("patientId", patientId);
            List<Bill> bills = query.getResultList();
            bills.forEach(bill -> Hibernate.initialize(bill.getItems()));
            return bills;
        });
    }

    @Override
    public List<Bill> findByPaymentStatus(PaymentStatus status) {
        return execute(session -> {
            if (status == null) return Collections.emptyList();
            Query<Bill> query = session.createQuery(
                    "SELECT b FROM Bill b LEFT JOIN FETCH b.patient WHERE b.paymentStatus = :status ORDER BY b.billDate DESC", Bill.class);
            query.setParameter("status", status);
            return query.getResultList();
        });
    }

    @Override
    public List<Bill> findAllUnpaid() {
        return execute(session -> {
            Query<Bill> query = session.createQuery(
                    "SELECT b FROM Bill b LEFT JOIN FETCH b.patient WHERE b.paymentStatus != :paidStatus ORDER BY b.billDate DESC", Bill.class);
            query.setParameter("paidStatus", PaymentStatus.PAID);
            List<Bill> bills = query.getResultList();
            for (Bill bill : bills) {
                Hibernate.initialize(bill.getItems());
                Hibernate.initialize(bill.getAppointment());
                Hibernate.initialize(bill.getPatient());
            }
            return bills;
        });
    }

    @Override
    public Optional<Bill> findByAppointmentId(Long appointmentId) {
        return execute(session -> {
            if (appointmentId == null) return Optional.empty();
            Query<Bill> query = session.createQuery(
                    "FROM Bill b WHERE b.appointment.appointmentId = :appointmentId", Bill.class);
            query.setParameter("appointmentId", appointmentId);
            return query.uniqueResultOptional();
        });
    }
    @Override
    public Optional<Bill> findById(Long id) {
        return execute(session -> {
            if (id == null) {
                return Optional.empty();
            }

            Bill bill = session.get(Bill.class, id);
            if (bill != null) {
                Hibernate.initialize(bill.getPatient());
                Hibernate.initialize(bill.getAppointment());
                Hibernate.initialize(bill.getItems());
                // initialize lazy collection
            }

            return Optional.ofNullable(bill);
        });
    }
}
