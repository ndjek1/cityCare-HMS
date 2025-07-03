package org.pahappa.systems.hms.dao;

import org.pahappa.systems.hms.models.Prescription;

import java.util.List;

public interface PrescriptionDao extends GenericDao<Prescription, Long> {

    public List<Prescription> getPrescriptionsForPatient(Long patientId);
    public List<Prescription> getPrescriptionsForAppointment(Long appointmentId);
}
