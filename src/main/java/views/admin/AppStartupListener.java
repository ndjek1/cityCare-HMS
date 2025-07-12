package views.admin;


import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.pahappa.systems.hms.constants.HospitalDepartment;
import org.pahappa.systems.hms.constants.UserRole;
import org.pahappa.systems.hms.services.AuthService;
import org.pahappa.systems.hms.services.StaffService;
import org.pahappa.systems.hms.services.impl.AuthServiceImpl;
import org.pahappa.systems.hms.services.impl.StaffServiceImpl;

import java.util.Date;

@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("==== Application starting up... ====");

        StaffServiceImpl staffService = new StaffServiceImpl();
        AuthServiceImpl authService = new AuthServiceImpl();
        if (!authService.usernameExists("Admin")) {
            staffService.registerStaff(
                    "Admin",
                    "Admin@123",
                    UserRole.ADMINISTRATOR,
                    "Administrator",
                    HospitalDepartment.ADMINISTRATION,
                    "Kampala",
                    "admin@citycare.com",
                    "0706080193",
                    new Date(989193600000L),
                    "Male"
            );
            System.out.println("✅ Default admin user registered.");
        } else {
            System.out.println("ℹ️ Admin user already exists.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("==== Application shutting down... ====");
    }
}

