package org.pahappa.systems.hms.services;

import org.pahappa.systems.hms.models.Prescription;

import java.util.List;
import java.util.Optional;

public interface PrescriptionService {

    Optional<Prescription> registerPrescription(Prescription prescription);
    List<Prescription> findByPatientId(Long id);
    List<Prescription> findByAppointmentId(Long id);
}
