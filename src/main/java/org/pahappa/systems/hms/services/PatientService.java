package org.pahappa.systems.hms.services;

import org.pahappa.systems.hms.models.Patient;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PatientService {
    Optional<Patient> registerPatient(String username, String password, String name, Date dob, String address, String phone, String email);
    Optional<Patient> findPatientById(Long patientId);
    List<Patient> getAllPatients();
    boolean updatePatientRecord(Long patientId, String address, String phone, String email, String insuranceDetails);
    void deletePatient(Patient patient);
}