package org.pahappa.systems.hms.constants;

public enum HospitalDepartment {
    // Clinical Departments
    GENERAL_MEDICINE("General Medicine"),
    INTERNAL_MEDICINE("Internal Medicine"),
    SURGERY("General Surgery"),
    ORTHOPEDICS("Orthopedics"),
    CARDIOLOGY("Cardiology"),
    NEUROLOGY("Neurology"),
    ONCOLOGY("Oncology"),
    RADIOLOGY("Radiology / Imaging"),


    // Non-Clinical / Administrative / Support Departments
    ADMINISTRATION("Administration / Management"),
    NURSING_SERVICES("Nursing Services"),

    // Other/Specialized
    DENTAL("Dental Clinic"),
    RESEARCH("Research and Development"),
    UNKNOWN("Unknown / Not Specified"); // Fallback

    private final String displayName;

    HospitalDepartment(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName; // Makes the enum print its display name by default
    }

    // Optional: A static method to get a department from its display name or a code
    public static HospitalDepartment fromDisplayName(String text) {
        for (HospitalDepartment dept : HospitalDepartment.values()) {
            if (dept.displayName.equalsIgnoreCase(text)) {
                return dept;
            }
        }
        // You might want to throw an IllegalArgumentException or return UNKNOWN
        // throw new IllegalArgumentException("No constant with display name " + text + " found");
        return UNKNOWN; // Return a default if not found
    }

}
