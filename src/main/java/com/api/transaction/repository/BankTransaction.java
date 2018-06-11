package com.api.transaction.repository;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Bean holding data about single transaction.
 */
@Entity
@Table(name = "bank_transaction")
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Double amount;

    @NotNull
    private Long timestamp;

    public BankTransaction(double amount, Long timestamp) {
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public BankTransaction() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
