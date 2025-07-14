package org.pahappa.systems.hms.services;

import org.pahappa.systems.hms.models.Appointment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentService {
    Optional<Appointment> bookAppointment(Long patientId, Long doctorId, LocalDateTime dateTime, String reason);
    boolean rescheduleAppointment(Long appointmentId, Long newDoctorId, LocalDateTime newDateTime);
    boolean cancelAppointment(Long appointmentId);
    boolean recordDiagnosis(Long appointmentId, String diagnosisNotes);
    List<Appointment> getAppointmentsForDoctor(Long doctorId);
    List<Appointment> getAppointmentsForPatient(Long patientId);
    Optional<Appointment> findAppointmentById(Long appointmentId);
    List<Appointment> findCompletedAppointments();
    long getTodaysScheduledAppointmentCount();
}