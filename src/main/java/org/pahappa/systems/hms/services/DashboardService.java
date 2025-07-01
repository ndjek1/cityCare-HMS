package org.pahappa.systems.hms.services;


import org.pahappa.systems.hms.models.Bill;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    /**
     * Gets the total number of registered patients.
     * @return A long representing the count.
     */
    long getPatientCount();

    /**
     * Gets the total number of active staff members.
     * @return A long representing the count.
     */
    long getStaffCount();

    /**
     * Gets the number of appointments scheduled for today.
     * @return A long representing the count.
     */
    long getTodaysScheduledAppointmentCount();

    /**
     * Gets the number of unpaid or partially paid bills.
     * @return A long representing the count.
     */
    long getOpenBillsCount();
    List<Bill> getAllBills();

    /**
     * Gets data for a chart showing patient registrations over the last 7 days.
     * @return A Map where keys are dates (as Strings) and values are counts (as Numbers).
     */
    Map<String, Number> getWeeklyPatientRegistrationData();
}
