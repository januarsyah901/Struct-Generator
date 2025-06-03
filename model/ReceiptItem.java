package model;

import java.math.BigDecimal;

public class ReceiptItem {
    private int id;
    private int receiptId;
    private String itemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public ReceiptItem() {
        this.quantity = 1;
        this.unitPrice = BigDecimal.ZERO;
        this.totalPrice = BigDecimal.ZERO;
    }

    public ReceiptItem(String itemName, int quantity, BigDecimal unitPrice) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.calculateTotal();
    }

    // Calculate total price based on quantity and unit price
    public void calculateTotal() {
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(int receiptId) {
        this.receiptId = receiptId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotal();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotal();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}