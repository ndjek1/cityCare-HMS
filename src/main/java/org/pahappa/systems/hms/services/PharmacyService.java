package org.pahappa.systems.hms.services;


import org.pahappa.systems.hms.models.Medication;

import java.util.List;
import java.util.Optional;

public interface PharmacyService {

    /**
     * Retrieves a list of all active medications available in the pharmacy catalog.
     * @return A List of active Medication objects.
     */
    List<Medication> getActiveMedications();

    /**
     * Retrieves the complete medication catalog, including inactive items (for admin purposes).
     * @return A List of all Medication objects.
     */
    List<Medication> getFullMedicationCatalog();

    /**
     * Finds a specific medication by its primary key ID.
     * @param medicationId The ID of the medication to find.
     * @return An Optional containing the Medication if found.
     */
    Optional<Medication> findMedicationById(Long medicationId);

    /**
     * Adds a new medication to the catalog.
     * @param name The name of the medication (e.g., "Paracetamol 500mg").
     * @param description A brief description.
     * @param unitPrice The price per unit.
     * @param initialStock The initial stock level.
     * @return An Optional containing the newly created Medication.
     */
    Optional<Medication> addNewMedication(String name, String description, double unitPrice, int initialStock);

    /**
     * Updates the stock level of a medication.
     * Can be used for dispensing (negative adjustment) or restocking (positive adjustment).
     * @param medicationId The ID of the medication to update.
     * @param quantityChange The amount to change the stock by (e.g., -10 for dispensing, +100 for restocking).
     * @return true if the stock update was successful, false otherwise.
     */
    boolean updateStockLevel(Long medicationId, int quantityChange);
    void updateMedication(Medication medication);

}
