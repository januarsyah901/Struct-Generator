package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Receipt {
    private int id;
    private String receiptNumber;
    private int customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String notes;
    private Date createdAt;
    private List<ReceiptItem> items;

    public Receipt() {
        this.items = new ArrayList<>();
        this.createdAt = new Date();
        this.totalAmount = BigDecimal.ZERO;
    }

    // Generates a receipt number based on timestamp
    public void generateReceiptNumber() {
        long timestamp = System.currentTimeMillis();
        this.receiptNumber = "REC-" + timestamp;
    }

    // Add an item to the receipt
    public void addItem(ReceiptItem item) {
        this.items.add(item);
        this.calculateTotal();
    }

    // Remove an item from the receipt
    public void removeItem(ReceiptItem item) {
        this.items.remove(item);
        this.calculateTotal();
    }

    // Calculate the total amount from all items
    public void calculateTotal() {
        this.totalAmount = BigDecimal.ZERO;
        for (ReceiptItem item : items) {
            this.totalAmount = this.totalAmount.add(item.getTotalPrice());
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<ReceiptItem> getItems() {
        return items;
    }

    public void setItems(List<ReceiptItem> items) {
        this.items = items;
        calculateTotal();
    }
}