package org.example.infrastructure;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ledger")
public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String paymentId;
    private String operationType;
    private BigDecimal amount;
    private Instant timestamp;

    protected LedgerEntry() {}

    public LedgerEntry(String paymentId, String operationType, BigDecimal amount) {
        this.paymentId = paymentId;
        this.operationType = operationType;
        this.amount = amount;
        this.timestamp = Instant.now();
    }

    public Long getId() { return id; }
    public String getPaymentId() { return paymentId; }
    public String getOperationType() { return operationType; }
    public BigDecimal getAmount() { return amount; }
    public Instant getTimestamp() { return timestamp; }
}