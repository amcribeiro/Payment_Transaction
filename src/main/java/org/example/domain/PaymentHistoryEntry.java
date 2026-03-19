package org.example.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_history")
public class PaymentHistoryEntry {
    @Id
    private String id = UUID.randomUUID().toString();
    private String paymentId;
    private String fromState;
    private String toState;
    private Instant createdAt = Instant.now();

    protected PaymentHistoryEntry() {}
    public PaymentHistoryEntry(String paymentId, String fromState, String toState) {
        this.paymentId = paymentId;
        this.fromState = fromState;
        this.toState = toState;
    }
}