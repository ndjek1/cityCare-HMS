package org.pahappa.systems.hms.dao;


import org.pahappa.systems.hms.models.Prescription;

import java.util.List;

public interface PrescriptionDao extends GenericDao<Prescription, Long> {
    List<Prescription> findByPatientId(Long patientId);
    List<Prescription> findByAppointmentId(Long appointmentId);
    List<Prescription> findAllUnpaid();
    public List<Prescription> findByDoctorId(Long doctorId);
}