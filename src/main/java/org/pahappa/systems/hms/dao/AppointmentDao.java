package org.pahappa.systems.hms.dao;

import org.pahappa.systems.hms.constants.AppointmentStatus;
import org.pahappa.systems.hms.models.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentDao extends GenericDao<Appointment, Long> {

    public List<Appointment> findByDateAndStatus(LocalDate date, AppointmentStatus status);
    public List<Appointment> findByStatus( AppointmentStatus status);
    public List<Appointment> getAppointmentsForDoctor(Long doctorId);
    boolean recordDiagnosis(Long appointmentId, String diagnosisNotes);
    boolean rescheduleAppointment(Long appointmentId, Long newDoctorId, LocalDateTime newDateTime);
    boolean cancelAppointment(Long appointmentId);
    Optional<Appointment> findAppointmentById(Long appointmentId);
    List<Appointment> getAppointmentsForPatient(Long patientId);
}
