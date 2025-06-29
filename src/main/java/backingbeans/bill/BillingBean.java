package backingbeans.bill;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import models.*; // Import ServiceCatalogItem, BillItem

import services.impl.AppointmentServiceImpl;
import services.impl.BillingServiceImpl;
import services.impl.ServiceCatalogServiceImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Named("billingBean")
@ViewScoped
public class BillingBean implements Serializable {

    private final ServiceCatalogServiceImpl serviceCatalogService;
    private final AppointmentServiceImpl appointmentService;
    private final BillingServiceImpl billingService;

    private Appointment appointmentForBill;
    private Appointment appointmentDetails;
    private Patient billToPatient;

    private List<BillItem> currentBillItems; // Items currently on the bill being built
    private List<ServiceCatalogItem> availableCatalogServices; // Predefined services
    private Long selectedCatalogServiceId; // For dropdown to select a predefined service
    private int quantityForSelectedItem = 1; // For selected catalog service

    private Bill generatedBill;

    public BillingBean() {
        this.serviceCatalogService = new ServiceCatalogServiceImpl();
        this.appointmentService = new AppointmentServiceImpl();
        this.billingService = new BillingServiceImpl();
    }

    @PostConstruct
    public void init() {
        currentBillItems = new ArrayList<>();
        availableCatalogServices = serviceCatalogService.getActiveServiceCatalogItems();
        if (availableCatalogServices == null) availableCatalogServices = new ArrayList<>();
    }

    public void prepareBillDialog(Appointment appointment) {
        this.appointmentForBill = appointment;
        this.currentBillItems.clear();
        this.generatedBill = null;
        this.selectedCatalogServiceId = null;
        this.quantityForSelectedItem = 1;

        if (appointment != null) {
            Optional<Appointment> optApp = appointmentService.findAppointmentById(appointment.getAppointmentId());
            if (optApp.isPresent()) {
                this.appointmentDetails = optApp.get();
                this.billToPatient = this.appointmentDetails.getPatient();
                // Automatically add a consultation fee if it's standard
                ServiceCatalogItem consultation = findServiceInCatalogByName("Doctor Consultation"); // Or by code
                if (consultation != null) {
                    currentBillItems.add(new BillItem(consultation.getName(), consultation.getDefaultCost(), 1));
                }
            } else {
                this.appointmentDetails = null;
                this.billToPatient = null;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Appointment details not found."));
            }
        }
    }

    // Helper to find a service by name (or code) for auto-adding
    private ServiceCatalogItem findServiceInCatalogByName(String name) {
        return availableCatalogServices.stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public void addSelectedCatalogItemToBill() {
        if (selectedCatalogServiceId == null) {
            FacesContext.getCurrentInstance().addMessage("billDialogMessages",
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "No Service Selected", "Please select a service from the catalog."));
            return;
        }
        if (quantityForSelectedItem <= 0) {
            FacesContext.getCurrentInstance().addMessage("billDialogMessages",
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Invalid Quantity", "Quantity must be greater than 0."));
            return;
        }

        Optional<ServiceCatalogItem> selectedServiceOpt = availableCatalogServices.stream()
                .filter(s -> s.getServiceId().equals(selectedCatalogServiceId))
                .findFirst();

        if (selectedServiceOpt.isPresent()) {
            ServiceCatalogItem service = selectedServiceOpt.get();
            // Check if item with same name already exists, if so, maybe update quantity or prevent
            Optional<BillItem> existingItem = currentBillItems.stream()
                    .filter(bi -> bi.getServiceName().equals(service.getName()))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Option: Update quantity of existing item
                // existingItem.get().setQuantity(existingItem.get().getQuantity() + quantityForSelectedItem);
                // For simplicity now, let's just add as a new line or prevent
                FacesContext.getCurrentInstance().addMessage("billDialogMessages",
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Item Exists", service.getName() + " is already on the bill. Remove it to add with a new quantity."));
            } else {
                currentBillItems.add(new BillItem(service.getName(), service.getDefaultCost(), quantityForSelectedItem));
            }
            selectedCatalogServiceId = null; // Reset dropdown
            quantityForSelectedItem = 1;     // Reset quantity
        } else {
            FacesContext.getCurrentInstance().addMessage("billDialogMessages",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Selected service not found in catalog."));
        }
    }

    public void removeBillItem(BillItem item) {
        currentBillItems.remove(item);
    }

    public void generateBillAction() {
        System.out.println("BillingBean: generateBillAction() ENTERED. Appt ID: " + this.appointmentForBill);
        FacesContext context = FacesContext.getCurrentInstance();
        if (appointmentForBill == null) { /* ... */ return; }
        if (currentBillItems.isEmpty()) {
            context.addMessage("billDialogMessages", new FacesMessage(FacesMessage.SEVERITY_WARN, "No Services", "Please add at least one service item."));
            return;
        }

        // Pass the constructed list of BillItems
        Optional<Bill> billOpt = billingService.generateBill(appointmentForBill, new ArrayList<>(currentBillItems));

        if (billOpt.isPresent()) {
            this.generatedBill = billOpt.get();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                    "Bill (ID: " + generatedBill.getBillId() + ") generated successfully for " + billToPatient.getName()));
        } else {
            this.generatedBill = null;
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Billing Failed",
                    "Could not generate bill. Check if bill already exists or appointment status."));
        }
    }

    // Getters and Setters
    public Appointment getAppointmentForBill() { return appointmentForBill; }
    public void setAppointmentIdForBill(Appointment appointmentForBill) { this.appointmentForBill = appointmentForBill; }
    public Appointment getAppointmentDetails() { return appointmentDetails; }
    public Patient getBillToPatient() { return billToPatient; }
    public List<BillItem> getCurrentBillItems() { return currentBillItems; }
    public void setCurrentBillItems(List<BillItem> currentBillItems) { this.currentBillItems = currentBillItems; }
    public List<ServiceCatalogItem> getAvailableCatalogServices() { return availableCatalogServices; }
    public Long getSelectedCatalogServiceId() { return selectedCatalogServiceId; }
    public void setSelectedCatalogServiceId(Long selectedCatalogServiceId) { this.selectedCatalogServiceId = selectedCatalogServiceId; }
    public int getQuantityForSelectedItem() { return quantityForSelectedItem; }
    public void setQuantityForSelectedItem(int quantityForSelectedItem) { this.quantityForSelectedItem = quantityForSelectedItem; }
    public Bill getGeneratedBill() { return generatedBill; }

    public double getTotalBillAmount() {
        if (currentBillItems == null) return 0.0;
        return currentBillItems.stream().mapToDouble(BillItem::getTotalCost).sum();
    }
}