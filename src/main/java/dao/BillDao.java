package dao;

import constants.PaymentStatus;
import models.Bill;

import java.util.List;
import java.util.Optional;

public interface BillDao extends GenericDao<Bill, Long> {
    public List<Bill> findByPatientId(Long patientId);
    public List<Bill> findByPaymentStatus(PaymentStatus status);
    public List<Bill> findAllUnpaid();
    public Optional<Bill> findByAppointmentId(Long appointmentId);
}
