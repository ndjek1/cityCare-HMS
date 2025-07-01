package views.bill;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import org.pahappa.systems.hms.models.Bill;

import org.pahappa.systems.hms.services.impl.BillingServiceImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("billBean")
@SessionScoped
public class BillBean implements Serializable {
    private final BillingServiceImpl billingService;

    private List<Bill> allUnpaidBills;

    public BillBean() {
        this.billingService = new BillingServiceImpl();
    }

    @PostConstruct
    public void init() {
        loadAllUnpaidBills();
    }

    public void loadAllUnpaidBills() {
        this.allUnpaidBills = billingService.getAllUnpaidBills();
        if (this.allUnpaidBills == null) { // Defensive
            this.allUnpaidBills = new ArrayList<>();
        }
    }

    public List<Bill> getAllUnpaidBills() {
        return allUnpaidBills;
    }
}
