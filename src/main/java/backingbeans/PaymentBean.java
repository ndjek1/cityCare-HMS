package backingbeans;


import constants.PaymentMethod;
import jakarta.inject.Named;
import models.Bill;

import java.time.LocalDateTime;

@Named("paymentBean")
public class PaymentBean {

    private Bill billId;
    private double amountPaid;
    private PaymentMethod method;
    private LocalDateTime paymentDate;

    public Bill getBillId() {
        return billId;
    }

    public void setBillId(Bill billId) {
        this.billId = billId;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
}
