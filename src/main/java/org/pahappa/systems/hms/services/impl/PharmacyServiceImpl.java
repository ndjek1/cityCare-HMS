package org.pahappa.systems.hms.services.impl;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.pahappa.systems.hms.dao.MedicationDao;
import org.pahappa.systems.hms.dao.impl.MedicationDaoImpl;
import org.pahappa.systems.hms.models.Medication;
import org.pahappa.systems.hms.services.PharmacyService;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Named("pharmacyService")
@ApplicationScoped
public class PharmacyServiceImpl implements PharmacyService {


    private final MedicationDaoImpl medicationDao;

    public PharmacyServiceImpl() {
        this.medicationDao = new MedicationDaoImpl();
    }

    @Override
    public List<Medication> getActiveMedications() {
        return medicationDao.findActiveMedications();
    }

    @Override
    public List<Medication> getFullMedicationCatalog() {
        return medicationDao.findAll();
    }

    @Override
    public Optional<Medication> findMedicationById(Long medicationId) {
        return medicationDao.findById(medicationId);
    }

    @Override
    public Optional<Medication> addNewMedication(String name, String description, double unitPrice, int initialStock) {
        // Business Rule: Don't allow adding a medication with a name that already exists.
        if (medicationDao.findByName(name).isPresent()) {
            System.err.println("SERVICE: A medication with the name '" + name + "' already exists.");
            return Optional.empty();
        }

        try {
            Medication newMedication = new Medication();
            newMedication.setName(name);
            newMedication.setDescription(description);
            newMedication.setUnitPrice(BigDecimal.valueOf(unitPrice)); // Convert double to BigDecimal
            newMedication.setStockLevel(initialStock);
            newMedication.setActive(true); // New medications are active by default

            medicationDao.save(newMedication);
            return Optional.of(newMedication);
        } catch (Exception e) {
            System.err.println("SERVICE ERROR: Could not add new medication.");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public boolean updateStockLevel(Long medicationId, int quantityChange) {
        Optional<Medication> medOpt = medicationDao.findById(medicationId);
        if (medOpt.isEmpty()) {
            System.err.println("SERVICE: Cannot update stock for non-existent medication ID: " + medicationId);
            return false;
        }

        Medication medication = medOpt.get();

        // Business Rule: Don't allow stock to go below zero when dispensing.
        if (quantityChange < 0 && (medication.getStockLevel() + quantityChange < 0)) {
            System.err.println("SERVICE: Insufficient stock for " + medication.getName() +
                    ". Required: " + Math.abs(quantityChange) + ", Available: " + medication.getStockLevel());
            return false;
        }

        medication.setStockLevel(medication.getStockLevel() + quantityChange);

        try {
            medicationDao.update(medication);
            System.out.println("SERVICE: Stock for " + medication.getName() + " updated to " + medication.getStockLevel());
            return true;
        } catch (Exception e) {
            System.err.println("SERVICE ERROR: Failed to update stock for medication ID: " + medicationId);
            e.printStackTrace();
            return false;
        }
    }

    // In PharmacyServiceImpl.java
    @Override
    public void updateMedication(Medication medication) {
        if (medication != null && medication.getMedicationId() != null) {
            // The DAO's update method uses merge, which is appropriate here.
            medicationDao.update(medication);
        }
    }
}