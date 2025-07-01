package org.pahappa.systems.hms.services.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.pahappa.systems.hms.constants.AppointmentStatus;
import org.pahappa.systems.hms.dao.AppointmentDao;
import org.pahappa.systems.hms.dao.BillDao;
import org.pahappa.systems.hms.dao.PatientDao;
import org.pahappa.systems.hms.dao.StaffDao;
import org.pahappa.systems.hms.dao.impl.AppointmentDaoImpl;
import org.pahappa.systems.hms.dao.impl.BillDaoImpl;
import org.pahappa.systems.hms.dao.impl.PatientDaoImpl;
import org.pahappa.systems.hms.dao.impl.StaffDaoImpl;
import org.pahappa.systems.hms.models.Bill;
import org.pahappa.systems.hms.services.DashboardService;
import utils.HibernateUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named("dashboardService")
@ApplicationScoped
public class DashboardServiceImpl implements DashboardService {

  private final SessionFactory factory; // Use factory directly for custom queries
  private final PatientDao patientDao;
  private final StaffDao staffDao;
  private final AppointmentDao appointmentDao;
  private final  BillDao billDao;


  public DashboardServiceImpl() {
      this.factory =  HibernateUtil.getSessionFactory();
      this.billDao = new BillDaoImpl();
      this.patientDao = new PatientDaoImpl();
      this.staffDao = new StaffDaoImpl();
      this.appointmentDao = new AppointmentDaoImpl();
  }
    @Override
    public long getPatientCount() {
        // The DAO's findAll().size() is an option, but a COUNT(*) query is more efficient.
        // Let's add a quick COUNT query here.
        try (Session session = factory.openSession()) {
            Query<Long> query = session.createQuery("SELECT count(p.patientId) FROM Patient p", Long.class);
            return query.uniqueResultOptional().orElse(0L);
        }
    }

    @Override
    public long getStaffCount() {
        try (Session session = factory.openSession()) {
            Query<Long> query = session.createQuery("SELECT count(s.staffId) FROM Staff s", Long.class);
            return query.uniqueResultOptional().orElse(0L);
        }
    }

    @Override
    public long getTodaysScheduledAppointmentCount() {
        // Here we can use the specific DAO method
        return appointmentDao.findByDateAndStatus(LocalDate.now(), AppointmentStatus.SCHEDULED).size();
    }

    @Override
    public long getOpenBillsCount() {
        // Use the DAO method
        return billDao.findAllUnpaid().size();
    }
    @Override
    public List<Bill> getAllBills(){
      return billDao.findAll();
    }


    @Override
    public Map<String, Number> getWeeklyPatientRegistrationData() {
        // This requires a more complex query that groups by date.
        // HQL doesn't have great date truncation functions that are portable,
        // so sometimes a native query is easier, but let's try with HQL first.
        // This is a simplified example; a real one might need more complex date handling.
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(6);
        Map<String, Number> dailyData = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        // Initialize map with all 7 days to ensure days with 0 registrations are shown
        for (int i = 0; i < 7; i++) {
            dailyData.put(sevenDaysAgo.plusDays(i).format(formatter), 0);
        }

        try (Session session = factory.openSession()) {
            // Assuming Patient entity has a 'registrationDate' field of type LocalDate or LocalDateTime
            Query<Object[]> query = session.createQuery(
                    "SELECT FUNCTION('DATE', p.registrationDate), COUNT(p.patientId) " +
                            "FROM Patient p " +
                            "WHERE p.registrationDate >= :startDate " +
                            "GROUP BY FUNCTION('DATE', p.registrationDate) " +
                            "ORDER BY FUNCTION('DATE', p.registrationDate)", Object[].class
            );
            query.setParameter("startDate", sevenDaysAgo.atStartOfDay()); // Convert to LocalDateTime if needed

            List<Object[]> results = query.getResultList();
            for (Object[] result : results) {
                LocalDate date = ((java.sql.Date) result[0]).toLocalDate(); // Convert from sql.Date
                Long count = (Long) result[1];
                dailyData.put(date.format(formatter), count);
            }
        } catch(Exception e) {
            e.printStackTrace();
            // Return empty or default map on error
        }
        return dailyData;
    }
}