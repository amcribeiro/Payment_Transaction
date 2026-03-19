package org.example.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "operations")
public class Operation {
    @Id
    private String id;
    private String paymentId;

    @Enumerated(EnumType.STRING)
    private OperationType type;

    @Enumerated(EnumType.STRING)
    private OperationStatus status;

    private BigDecimal amount;
    private String transRef;

    @Enumerated(EnumType.STRING)
    private ReversalTarget target;
    private String reason;

    private LocalDateTime createdAt;

    protected Operation() {}

    public Operation(String paymentId, OperationType type, BigDecimal amount, String transRef, ReversalTarget target, String reason) {
        this.id = UUID.randomUUID().toString();
        this.paymentId = paymentId;
        this.type = type;
        this.amount = amount;
        this.transRef = transRef;
        this.target = target;
        this.reason = reason;
        this.status = OperationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public OperationStatus getStatus() { return status; }
    public void setStatus(OperationStatus status) { this.status = status; }
    public OperationType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public ReversalTarget getTarget() { return target; }
    public String getReason() { return reason; }
}