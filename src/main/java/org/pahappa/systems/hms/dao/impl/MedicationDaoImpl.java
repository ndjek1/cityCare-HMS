package org.pahappa.systems.hms.dao.impl;

import jakarta.enterprise.context.ApplicationScoped;

import org.hibernate.query.Query;
import org.pahappa.systems.hms.dao.MedicationDao;
import org.pahappa.systems.hms.models.Medication;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MedicationDaoImpl extends AbstractDao<Medication, Long> implements MedicationDao {

    public MedicationDaoImpl() {
        super(Medication.class);
    }

    @Override
    public Optional<Medication> findByName(String name) {
        return execute(session -> {
            if (name == null || name.trim().isEmpty()) return Optional.empty();
            Query<Medication> query = session.createQuery(
                    "FROM Medication m WHERE m.name = :name", Medication.class);
            query.setParameter("name", name.trim());
            return query.uniqueResultOptional();
        });
    }

    @Override
    public List<Medication> findActiveMedications() {
        return execute(session -> {
            try {
                Query<Medication> query = session.createQuery(
                        "FROM Medication m WHERE m.active = true ORDER BY m.name", Medication.class);
                return query.getResultList();
            } catch (Exception e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        });
    }
}
