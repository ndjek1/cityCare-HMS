package org.pahappa.systems.hms.services;

import org.pahappa.systems.hms.models.PrescribedMedication;
import org.pahappa.systems.hms.models.Prescription;

import java.util.List;
import java.util.Optional;

public interface PrescriptionService {

    boolean createPrescription(Long appointmentId, List<PrescribedMedication> items);
    List<Prescription> findByPatientId(Long id);
    List<Prescription> findByAppointmentId(Long id);
    List<Prescription> findAllUnpaid();
}
