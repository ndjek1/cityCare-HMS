package views.admin; // Suggested package

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.Min;
import org.pahappa.systems.hms.models.Patient;
import org.pahappa.systems.hms.models.ServiceCatalogItem;
import org.pahappa.systems.hms.constants.ServiceCategory; // Your enum

import org.pahappa.systems.hms.services.impl.ServiceCatalogServiceImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Named("serviceCatalogAdminBean")
@ViewScoped // Good for admin forms
public class ServiceCatalogAdminBean implements Serializable {

    private final ServiceCatalogServiceImpl  serviceCatalogService;

    private List<ServiceCatalogItem> catalogItems; // To display existing items
    private List<ServiceCatalogItem> filteredCatalogItems;

    // Fields for adding a new service
    private String newServiceCode;
    private String newServiceName;
    @Min(value = 0,message = "Cost cannot be less than 0")
    private double newServiceDefaultCost;
    private String newServiceDescription;
    private ServiceCategory newServiceCategory;
    private boolean newServiceActive = true; // Default to active

    public ServiceCatalogAdminBean() {
        this.serviceCatalogService = new ServiceCatalogServiceImpl();
    }

    @PostConstruct
    public void init() {
        loadCatalogItems();
        resetNewServiceForm();
    }

    public void loadCatalogItems() {
        // Modify HospitalService to have a method that gets ALL items, not just active
        // or create a separate method for admin purposes.
        // For now, let's assume a method getFullServiceCatalog()
        catalogItems = serviceCatalogService.getFullServiceCatalog(); // You'll need to create this method
        if (catalogItems == null) {
            catalogItems = new ArrayList<>();
        }
        this.filteredCatalogItems = new ArrayList<>(catalogItems);
    }

    private void resetNewServiceForm() {
        newServiceCode = null;
        newServiceName = null;
        newServiceDefaultCost = 0;
        newServiceDescription = null;
        newServiceCategory = null; // Or a default category
        newServiceActive = true;
    }


    // ✅ Global Filter Function
    public boolean globalFilterFunction(Object value, Object filter, java.util.Locale locale) {
        String filterText = (filter == null) ? "" : filter.toString().toLowerCase();
        if (filterText.isBlank()) return true;

        ServiceCatalogItem serviceCatalogItem = (ServiceCatalogItem) value;

        return (serviceCatalogItem.getName() != null && serviceCatalogItem.getName().toLowerCase().contains(filterText)) ||
                (serviceCatalogItem.getServiceCode() != null && serviceCatalogItem.getServiceCode().toLowerCase().contains(filterText)) ||
                (serviceCatalogItem.getCategory().toString() != null && serviceCatalogItem.getCategory().toString().toLowerCase().contains(filterText)) ||
                (serviceCatalogItem.getDescription() != null && serviceCatalogItem.getDescription().toLowerCase().contains(filterText));
    }
    public void addNewServiceCatalogItem() {
        FacesContext context = FacesContext.getCurrentInstance();
        // Basic Validation
        if (newServiceName == null || newServiceName.trim().isEmpty() ||
                newServiceDefaultCost == 0 || newServiceDefaultCost <= 0 ||
                newServiceCategory == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error",
                    "Service Name, Default Cost, and Category are required. Cost must be positive."));
            return;
        }
        if (newServiceCode != null && newServiceCode.trim().isEmpty()) {
            newServiceCode = null; // Treat empty code as null
        }

        // Optional: Check if service code or name already exists in hospitalService before adding
        // if (newServiceCode != null && hospitalService.serviceCodeExists(newServiceCode)) { ... add error ... return; }
        // if (hospitalService.serviceNameExists(newServiceName)) { ... add error ... return; }


        Optional<ServiceCatalogItem> addedItemOpt = serviceCatalogService.addServiceToCatalog(
                newServiceCode,
                newServiceName,
                newServiceDefaultCost,
                newServiceCategory,
                newServiceDescription
                // Pass 'newServiceActive' if your service method supports it,
                // otherwise, new org.pahappa.systems.hms.navigation.services are active by default as per entity.
        );

        if (addedItemOpt.isPresent()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                    "Service '" + addedItemOpt.get().getName() + "' added to catalog."));
            loadCatalogItems(); // Refresh the list
            resetNewServiceForm(); // Clear the form
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "Could not add service to catalog. It might already exist or an error occurred."));
        }
    }
    public void deactivateService(ServiceCatalogItem serviceToDeactivate) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (serviceToDeactivate != null) {
            System.out.println("Attempting to delete staff: " + serviceToDeactivate.getName());
            boolean success = serviceCatalogService.deactivateService(serviceToDeactivate); // Assuming deleteStaff takes ID or object
            if (success) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Warning",
                        "Service '" + serviceToDeactivate.getName() + "' has  been deactivated."));
            }
            loadCatalogItems(); // Refresh the list after deletion
            // Add FacesMessage for success/failure
        }
    }

    public void activateService(ServiceCatalogItem serviceToReactivate) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (serviceToReactivate != null) {
            System.out.println("Attempting to delete staff: " + serviceToReactivate.getName());
           boolean success =  serviceCatalogService.activateService(serviceToReactivate); // Assuming deleteStaff takes ID or object
            if (success) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                        "Service '" + serviceToReactivate.getName() + "' has  been activated."));
            }
            loadCatalogItems(); // Refresh the list after deletion
            // Add FacesMessage for success/failure
        }
    }

    public void toggleServiceStatus(ServiceCatalogItem item) {
        if (item == null) return;

        if (item.isActive()) {
            deactivateService(item);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Service Deactivated",
                            item.getName() + " has been deactivated."));
        } else {
            activateService(item);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Service Activated",
                            item.getName() + " has been activated."));
        }
    }



    // Getters and Setters

    public List<ServiceCatalogItem> getFilteredCatalogItems() {
        return filteredCatalogItems;
    }

    public void setFilteredCatalogItems(List<ServiceCatalogItem> filteredCatalogItems) {
        this.filteredCatalogItems = filteredCatalogItems;
    }

    public List<ServiceCatalogItem> getCatalogItems() { return catalogItems; }
    public String getNewServiceCode() { return newServiceCode; }
    public void setNewServiceCode(String newServiceCode) { this.newServiceCode = newServiceCode; }
    public String getNewServiceName() { return newServiceName; }
    public void setNewServiceName(String newServiceName) { this.newServiceName = newServiceName; }
    public Double getNewServiceDefaultCost() { return newServiceDefaultCost; }
    public void setNewServiceDefaultCost(Double newServiceDefaultCost) { this.newServiceDefaultCost = newServiceDefaultCost; }
    public String getNewServiceDescription() { return newServiceDescription; }
    public void setNewServiceDescription(String newServiceDescription) { this.newServiceDescription = newServiceDescription; }
    public ServiceCategory getNewServiceCategory() { return newServiceCategory; }
    public void setNewServiceCategory(ServiceCategory newServiceCategory) { this.newServiceCategory = newServiceCategory; }
    public boolean isNewServiceActive() { return newServiceActive; }
    public void setNewServiceActive(boolean newServiceActive) { this.newServiceActive = newServiceActive; }

    // For dropdown
    public List<ServiceCategory> getAvailableServiceCategories() {
        return Arrays.asList(ServiceCategory.values());
    }
}