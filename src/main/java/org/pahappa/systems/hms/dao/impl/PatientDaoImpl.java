package org.pahappa.systems.hms.dao.impl;

import org.pahappa.systems.hms.dao.PatientDao;
import org.pahappa.systems.hms.models.Patient;
import org.hibernate.Hibernate;
import java.util.Optional;


public class PatientDaoImpl extends AbstractDao<Patient, Long> implements PatientDao {

    public PatientDaoImpl() {
        super(Patient.class);
    }

    @Override
    public Optional<Patient> findByIdWithHistory(Long id) {
        return execute(session -> {
            Optional<Patient> patientOpt = Optional.ofNullable(session.get(Patient.class, id));
            patientOpt.ifPresent(p -> Hibernate.initialize(p.getMedicalHistory()));
            return patientOpt;
        });
    }

    @Override
    public void delete(Patient entity) {
        execute(session -> {
            entity.setDeleted(true);
            session.merge(entity);
            return null;
        });
    }
}