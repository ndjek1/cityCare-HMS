package org.pahappa.systems.hms.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.pahappa.systems.hms.models.Patient;

import java.util.Optional;

public interface PatientDao extends GenericDao<Patient, Long> {
    public Optional<Patient> findByIdWithHistory(Long id);

     long getPatientCount() ;
}
