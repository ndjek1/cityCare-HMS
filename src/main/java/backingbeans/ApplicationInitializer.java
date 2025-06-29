package backingbeans; // Should ideally be in a more specific package like com.yourapp.startup

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import services.HospitalService;

@ApplicationScoped
public class ApplicationInitializer {

    @Inject
    private HospitalService hospitalService; // CDI will inject the @ApplicationScoped HospitalService

    public void onApplicationStartup(@Observes @Initialized(ApplicationScoped.class) Object eventPayload) {
        // This method is called automatically by CDI when the application starts.
        System.out.println("CDI Event: Application Scope Initialized. ApplicationInitializer.onApplicationStartup() called.");
        System.out.println("HospitalService injected: " + (hospitalService != null));

        // If you want to seed data (e.g., create a default admin IF IT DOESN'T EXIST),
        // you would call a method on hospitalService here.
        // For example:
        // if (hospitalService != null) {
        //     try {
        //         System.out.println("Attempting to ensure initial data/configuration...");
        //         hospitalService.ensureInitialAdminExists(); // Or seedInitialData() if it checks for existence
        //         System.out.println("Initial data/configuration check complete.");
        //     } catch (Exception e) {
        //         System.err.println("ERROR during initial data/configuration: " + e.getMessage());
        //         e.printStackTrace();
        //     }
        // } else {
        //     System.err.println("HospitalService was not injected, cannot perform initial data setup.");
        // }

        System.out.println("No automatic data seeding configured to run in ApplicationInitializer.");
    }
}