package org.pahappa.systems.hms.constants;

public enum PrescriptionStatus {
    PENDING("Pending Dispense"),
    DISPENSED("Dispensed"),
    CANCELLED("Cancelled");

    private final String displayName;

    PrescriptionStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
