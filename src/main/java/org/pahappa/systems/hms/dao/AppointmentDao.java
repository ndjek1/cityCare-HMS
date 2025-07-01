package org.pahappa.systems.hms.dao;

import org.pahappa.systems.hms.constants.AppointmentStatus;
import org.pahappa.systems.hms.models.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentDao extends GenericDao<Appointment, Long> {
    public List<Appointment> findByDoctorId(Long doctorId);
    public List<Appointment> findByPatientId(Long patientId);
    public List<Appointment> findByDateAndStatus(LocalDate date, AppointmentStatus status);
}
