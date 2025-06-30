package org.pahappa.systems.hms.constants; // Or your enum package

public enum ServiceCategory {
    CONSULTATION("Consultation"),
    LABORATORY("Laboratory Test"),
    RADIOLOGY("Radiology/Imaging"),
    PHARMACY("Pharmacy Item"),
    PROCEDURE("Medical Procedure"),
    ROOM_CHARGE("Room Charge"),
    OTHER("Other");

    private final String displayName;

    ServiceCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
