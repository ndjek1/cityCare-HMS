package org.pahappa.systems.hms.dao.impl;

import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.Hibernate;
import org.hibernate.query.Query;
import org.pahappa.systems.hms.constants.PaymentStatus;
import org.pahappa.systems.hms.dao.PrescriptionDao;
import org.pahappa.systems.hms.models.Bill;
import org.pahappa.systems.hms.models.Prescription;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class PrescriptionDaoImpl extends AbstractDao<Prescription, Long> implements PrescriptionDao {

    public PrescriptionDaoImpl() {
        super(Prescription.class);
    }

    @Override
    public List<Prescription> findByPatientId(Long patientId) {
        return execute(session -> {
            if (patientId == null) return Collections.emptyList();
            Query<Prescription> query = session.createQuery(
                    "SELECT p FROM Prescription p " +
                            "LEFT JOIN FETCH p.doctor " + // Fetch related data to avoid lazy loading issues
                            "WHERE p.patient.patientId = :patientId ORDER BY p.prescriptionDate DESC", Prescription.class);
            query.setParameter("patientId", patientId);
            List<Prescription> prescriptions = query.getResultList();
            // The items are EAGER fetched, but let's be safe if that ever changes
            prescriptions.forEach(pr -> Hibernate.initialize(pr.getPrescribedMedications()));
            return prescriptions;
        });
    }
    @Override
    public List<Prescription> findByDoctorId(Long doctorId) {
        return execute(session -> {
            if (doctorId == null) return Collections.emptyList();
            Query<Prescription> query = session.createQuery(
                    "SELECT p FROM Prescription p " +
                            "LEFT JOIN FETCH p.doctor " +
                            "WHERE p.doctor.id = :doctorId " +
                            "ORDER BY p.prescriptionDate DESC", Prescription.class);
            query.setParameter("doctorId", doctorId);
            List<Prescription> prescriptions = query.getResultList();
            prescriptions.forEach(pr -> Hibernate.initialize(pr.getPrescribedMedications()));
            return prescriptions;
        });
    }

    @Override
    public List<Prescription> findByAppointmentId(Long appointmentId) {
        return execute(session -> {
            if (appointmentId == null) return Collections.emptyList();
            Query<Prescription> query = session.createQuery(
                    "FROM Prescription p WHERE p.appointment.appointmentId = :appointmentId", Prescription.class);
            query.setParameter("appointmentId", appointmentId);
            List<Prescription> prescriptions = query.getResultList();
            prescriptions.forEach(pr -> Hibernate.initialize(pr.getPrescribedMedications()));
            return prescriptions;
        });
    }

    @Override
    public List<Prescription> findAllUnpaid(){
        return execute(session -> {
            Query<Prescription> query = session.createQuery(
                    "SELECT b FROM Prescription b LEFT JOIN FETCH b.patient WHERE b.paymentStatus != :paidStatus ORDER BY b.prescriptionDate DESC", Prescription.class);
            query.setParameter("paidStatus", PaymentStatus.PAID);
            List<Prescription> prescriptions = query.getResultList();
            for (Prescription prescription : prescriptions) {
                Hibernate.initialize(prescription.getPrescribedMedications());
                Hibernate.initialize(prescription.getAppointment());
                Hibernate.initialize(prescription.getPatient());
            }
            return prescriptions;
        });
    }
}