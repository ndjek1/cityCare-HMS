package utils;


import org.pahappa.systems.hms.models.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;


// ... add all other entity classes you created

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    // Static block to initialize SessionFactory once
    static {
        try {
            // 1. Create Configuration instance
            Configuration configuration = new Configuration();

            // 2. Configure from hibernate.cfg.xml
            configuration.configure();
            // 3. Add your annotated entity classes programmatically
            // This is an alternative or addition to <mapping class="..."/> in hibernate.cfg.xml
            configuration.addAnnotatedClass(Patient.class);
            configuration.addAnnotatedClass(Appointment.class);
            configuration.addAnnotatedClass(Bill.class);
            configuration.addAnnotatedClass(Payment.class); // Assuming Payment is an entity
            configuration.addAnnotatedClass(UserAccount.class); // Assuming UserAccount is an entity
            configuration.addAnnotatedClass(Staff.class);
            configuration.addAnnotatedClass(Appointment.class);
            configuration.addAnnotatedClass(ServiceCatalogItem.class);
            // Hibernate 5+ uses ServiceRegistry
            // If you are using Hibernate 4, the way to build SessionFactory is slightly different:
            // sessionFactory = configuration.buildSessionFactory(new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build());

            // For Hibernate 5.x and 6.x:
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();

            System.out.println("Hibernate Java Config serviceRegistry created");

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            System.out.println("Hibernate SessionFactory created successfully.");

        } catch (Throwable ex) {
            // Log the exception.
            System.err.println("Initial SessionFactory creation failed." + ex);
            ex.printStackTrace(); // Print stack trace for detailed error
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            // This should not happen if static block executed correctly
            // but as a fallback, you could try re-initializing or throw a specific error.
            // For simplicity, we assume static block handles initialization.
            System.err.println("SessionFactory is null. Re-check initialization.");
            // Optionally, re-trigger static block or handle this scenario.
            // For now, let's rely on the static block. If it fails, the ExceptionInInitializerError will be thrown.
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            getSessionFactory().close();
            System.out.println("Hibernate SessionFactory shut down.");
        }
    }
}
