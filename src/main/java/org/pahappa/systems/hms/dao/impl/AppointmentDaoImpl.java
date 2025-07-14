package org.pahappa.systems.hms.dao.impl;

import org.hibernate.Hibernate;
import org.hibernate.query.Query;
import org.pahappa.systems.hms.constants.AppointmentStatus;
import org.pahappa.systems.hms.dao.AppointmentDao;
import org.pahappa.systems.hms.models.Appointment;
import org.pahappa.systems.hms.models.Patient;
import org.pahappa.systems.hms.models.Staff;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AppointmentDaoImpl extends AbstractDao<Appointment, Long> implements AppointmentDao {

    public AppointmentDaoImpl() {
        super(Appointment.class);
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return execute(session -> {
            if (id == null) return Optional.empty();

            Appointment appointment = session.get(Appointment.class, id);
            if (appointment != null) {
                Hibernate.initialize(appointment.getPatient());
                Hibernate.initialize(appointment.getDoctor());
                Hibernate.initialize(appointment.getBill());
            }
            return Optional.ofNullable(appointment);
        });
    }

    @Override
    public List<Appointment> findByDateAndStatus(LocalDate date, AppointmentStatus status) {
        return execute(session -> {
            Query<Appointment> query = session.createQuery(
                    "FROM Appointment a WHERE FUNCTION('DATE', a.dateTime) = :date AND a.status = :status",
                    Appointment.class
            );
            query.setParameter("date", date);
            query.setParameter("status", status);
            return query.getResultList();
        });
    }

    @Override
    public List<Appointment> findAll() {
        return execute(session -> {
            List<Appointment> result = session.createQuery("FROM Appointment a", Appointment.class).getResultList();
            for (Appointment appointment : result) {
                Hibernate.initialize(appointment.getPatient());
                Hibernate.initialize(appointment.getDoctor());
                Hibernate.initialize(appointment.getBill());
            }
            return result;
        });
    }

    @Override
    public List<Appointment> findByStatus(AppointmentStatus status) {
        return execute(session -> {
            Query<Appointment> query = session.createQuery(
                    "FROM Appointment a WHERE a.status = :status", Appointment.class);
            query.setParameter("status", status);
            List<Appointment> appointments = query.getResultList();
            for (Appointment appointment : appointments) {
                Hibernate.initialize(appointment.getDoctor());
                Hibernate.initialize(appointment.getPatient());
            }
            return appointments;
        });
    }

    @Override
    public List<Appointment> getAppointmentsForDoctor(Long doctorId) {
        return execute(session -> {
            Query<Appointment> query = session.createQuery(
                    "SELECT a FROM Appointment a " +
                            "JOIN FETCH a.patient " +
                            "WHERE a.doctor.staffId = :doctorId ORDER BY a.dateTime DESC",
                    Appointment.class
            );
            query.setParameter("doctorId", doctorId);
            List<Appointment> appointments = query.getResultList();
            for (Appointment appointment : appointments) {
                Hibernate.initialize(appointment.getDoctor());
                Hibernate.initialize(appointment.getBill());
            }
            return appointments;
        });
    }

    @Override
    public boolean recordDiagnosis(Long appointmentId, String diagnosisNotes) {
        return execute(session -> {
            Appointment appointment = session.get(Appointment.class, appointmentId);
            if (appointment == null || appointment.getStatus() != AppointmentStatus.SCHEDULED) return false;

            appointment.setDiagnosisNotes(diagnosisNotes);
            appointment.setStatus(AppointmentStatus.COMPLETED);

            Patient patient = appointment.getPatient();
            if (patient != null) {
                String history = "Date: " + appointment.getDateTime().toLocalDate() + ", Diagnosis: " + diagnosisNotes;
                patient.addMedicalHistory(history);
            }

            session.merge(appointment);
            return true;
        });
    }

    @Override
    public boolean rescheduleAppointment(Long appointmentId, Long newDoctorId, LocalDateTime newDateTime) {
        return execute(session -> {
            Appointment appointment = session.get(Appointment.class, appointmentId);
            if (appointment == null ||
                    appointment.getStatus() == AppointmentStatus.CANCELLED ||
                    appointment.getStatus() == AppointmentStatus.COMPLETED)
                return false;

            Staff oldDoctor = appointment.getDoctor();
            LocalDateTime oldDateTime = appointment.getDateTime();

            Staff newDoctor = session.get(Staff.class, newDoctorId);
            if (newDoctor == null || newDoctor.getRole() != org.pahappa.systems.hms.constants.UserRole.DOCTOR)
                return false;

            if (oldDoctor != null && oldDateTime != null) {
                Hibernate.initialize(oldDoctor.getAvailableSlots());
                oldDoctor.addAvailability(oldDateTime);
                session.merge(oldDoctor);
            }

            Hibernate.initialize(newDoctor.getAvailableSlots());
            if (newDoctor.removeAvailability(newDateTime)) {
                appointment.setDoctor(newDoctor);
                appointment.setDateTime(newDateTime);
                appointment.setStatus(AppointmentStatus.SCHEDULED);
                session.merge(appointment);
                session.merge(newDoctor);
                return true;
            }

            return false;
        });
    }

    @Override
    public boolean cancelAppointment(Long appointmentId) {
        return execute(session -> {
            Appointment appointment = session.get(Appointment.class, appointmentId);
            if (appointment == null) return false;

            if (appointment.getStatus() == AppointmentStatus.CANCELLED) return true;
            if (appointment.getStatus() == AppointmentStatus.COMPLETED) return false;

            appointment.setStatus(AppointmentStatus.CANCELLED);

            Staff doctor = appointment.getDoctor();
            LocalDateTime appointmentDateTime = appointment.getDateTime();

            if (doctor != null && appointmentDateTime != null) {
                Hibernate.initialize(doctor.getAvailableSlots());
                doctor.addAvailability(appointmentDateTime);
                session.merge(doctor);
            }

            session.merge(appointment);
            return true;
        });
    }

    @Override
    public Optional<Appointment> findAppointmentById(Long appointmentId) {
        return execute(session -> {
            Appointment appointment = session.get(Appointment.class, appointmentId);
            if (appointment != null) {
                Hibernate.initialize(appointment.getPatient());
                Hibernate.initialize(appointment.getDoctor());
            }
            return Optional.ofNullable(appointment);
        });
    }

    @Override
    public List<Appointment> getAppointmentsForPatient(Long patientId) {
        return execute(session -> {
            Query<Appointment> query = session.createQuery(
                    "FROM Appointment a WHERE a.patient.patientId = :patientId", Appointment.class);
            query.setParameter("patientId", patientId);
            List<Appointment> appointments = query.getResultList();
            for (Appointment appointment : appointments) {
                Hibernate.initialize(appointment.getPatient());
                Hibernate.initialize(appointment.getDoctor());
            }
            return appointments;
        });
    }
}
