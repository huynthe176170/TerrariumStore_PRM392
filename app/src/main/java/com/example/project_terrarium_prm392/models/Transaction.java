package com.example.project_terrarium_prm392.models;

import java.util.Date;

public class Transaction {
    private int id;
    private int paymentId;
    private Payment payment;
    private String transactionType;
    private double amount;
    private String status;
    private Date transactionDate;
    private String description;

    // Constructors
    public Transaction() {
        this.transactionDate = new Date();
        this.status = "Pending";
    }

    public Transaction(int id, int paymentId, String transactionType, double amount, String description) {
        this.id = id;
        this.paymentId = paymentId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.transactionDate = new Date();
        this.status = "Pending";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
} 