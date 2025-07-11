package org.pahappa.systems.hms.dao;


import org.pahappa.systems.hms.models.Medication;

import java.util.List;
import java.util.Optional;

public interface MedicationDao extends GenericDao<Medication, Long> {
    Optional<Medication> findByName(String name);
    List<Medication> findActiveMedications();
}
