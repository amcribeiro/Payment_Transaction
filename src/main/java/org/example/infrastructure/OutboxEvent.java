package org.example.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    private String id;
    private int retryCount = 0;

    private java.time.Instant nextAttemptAt = java.time.Instant.now();

    private String paymentId;

    private String operationId;

    private String eventType;

    private String status;

    private Instant createdAt;

    protected OutboxEvent() {}

    public OutboxEvent(String paymentId, String operationId, String eventType) {
        this.id = UUID.randomUUID().toString();
        this.paymentId = paymentId;
        this.operationId = operationId;
        this.eventType = eventType;
        this.status = "NEW";
        this.createdAt = Instant.now();
    }


    public String getId() { return id; }
    public String getPaymentId() { return paymentId; }
    public String getEventType() { return eventType; }
    public String getStatus() { return status; }
    public String getOperationId() { return operationId; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public java.time.Instant getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(java.time.Instant nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }

    public void setStatus(String status) { this.status = status; }
}