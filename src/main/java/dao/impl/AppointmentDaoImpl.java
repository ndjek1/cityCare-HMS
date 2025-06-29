package dao.impl;


import constants.AppointmentStatus;
import dao.AppointmentDao;
import jakarta.enterprise.context.ApplicationScoped;
import models.Appointment;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


public class AppointmentDaoImpl extends AbstractDao<Appointment, Long> implements AppointmentDao {

    public AppointmentDaoImpl() {
        super(Appointment.class);
    }

    @Override
    public List<Appointment> findByDoctorId(Long doctorId) {
        return execute(session -> {
            if (doctorId == null) return Collections.emptyList();
            // Use JOIN FETCH to avoid N+1 problem on patient.name
            Query<Appointment> query = session.createQuery(
                    "SELECT a FROM Appointment a LEFT JOIN FETCH a.patient WHERE a.doctor.staffId = :doctorId ORDER BY a.dateTime DESC", Appointment.class);
            query.setParameter("doctorId", doctorId);
            return query.getResultList();
        });
    }

    @Override
    public List<Appointment> findByPatientId(Long patientId) {
        return execute(session -> {
            if (patientId == null) return Collections.emptyList();
            // Use JOIN FETCH to avoid N+1 problem on doctor.name
            Query<Appointment> query = session.createQuery(
                    "SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor WHERE a.patient.patientId = :patientId ORDER BY a.dateTime DESC", Appointment.class);
            query.setParameter("patientId", patientId);
            return query.getResultList();
        });
    }

    @Override
    public List<Appointment> findByDateAndStatus(LocalDate date, AppointmentStatus status) {
        return execute(session -> {
            Query<Appointment> query = session.createQuery(
                    "FROM Appointment a WHERE FUNCTION('DATE', a.dateTime) = :date AND a.status = :status", Appointment.class);
            query.setParameter("date", date);
            query.setParameter("status", status);
            return query.getResultList();
        });
    }
}
