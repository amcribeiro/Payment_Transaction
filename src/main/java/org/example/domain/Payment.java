package org.example.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private String id;
    private String merchantId;
    private String orderId;
    private String currency;
    private BigDecimal requestedAmount;
    private BigDecimal completedAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private PaymentState state;

    private BigDecimal preauthAmount = BigDecimal.ZERO;
    private BigDecimal topupAmount = BigDecimal.ZERO;
    private String transRef;

    @Version
    private Long version;

    protected Payment() { }

    public Payment(String merchantId, String orderId, String currency, BigDecimal requestedAmount) {
        this.id = UUID.randomUUID().toString();
        this.merchantId = merchantId;
        this.orderId = orderId;
        this.currency = currency;
        this.requestedAmount = requestedAmount;
        this.state = PaymentState.NEW;
    }

    public BigDecimal getAuthorizedTotal() {
        return preauthAmount.add(topupAmount);
    }

    public void requestPreauth(BigDecimal amount) {
        if (this.state != PaymentState.NEW) {
            throw new IllegalStateException("PREAUTH only allowed on NEW payments");
        }
        this.preauthAmount = amount;
        this.state = PaymentState.PREAUTH_PENDING;
    }

    public void requestTopup(BigDecimal amount, String requestTransRef) {
        if (this.state != PaymentState.PREAUTH_AUTHORIZED && this.state != PaymentState.TOPUP_AUTHORIZED) {
            throw new IllegalStateException("INVALID_STATE for TOPUP");
        }
        if (this.transRef == null || !this.transRef.equals(requestTransRef)) {
            throw new IllegalArgumentException("TRANSREF_MISMATCH");
        }
        this.state = PaymentState.TOPUP_PENDING;
    }

    public void requestCompletion(BigDecimal amount, String requestTransRef) {
        if (this.state != PaymentState.PREAUTH_AUTHORIZED && this.state != PaymentState.TOPUP_AUTHORIZED) {
            throw new IllegalStateException("INVALID_STATE for COMPLETION");
        }
        if (this.transRef == null || !this.transRef.equals(requestTransRef)) {
            throw new IllegalArgumentException("TRANSREF_MISMATCH");
        }
        if (amount.compareTo(getAuthorizedTotal()) > 0) {
            throw new IllegalArgumentException("INSUFFICIENT_AUTHORIZED_TOTAL");
        }
        this.completedAmount = amount;
        this.state = PaymentState.COMPLETION_PENDING;
    }

    public void requestSale(BigDecimal amount) {
        if (this.state != PaymentState.NEW) {
            throw new IllegalStateException("SALE only allowed on NEW payments");
        }
        this.preauthAmount = amount;
        this.completedAmount = amount;
        this.state = PaymentState.SALE_PENDING;
    }

    public void addTopupAmount(BigDecimal amount) {
        this.topupAmount = this.topupAmount.add(amount);
    }

    public void requestReversal(ReversalTarget targetType) {
        if (targetType == ReversalTarget.PREAUTH) {
            if (this.state == PaymentState.COMPLETED) {
                throw new IllegalStateException("Cannot reverse PREAUTH if already COMPLETED");
            }
            this.state = PaymentState.PREAUTH_REVERSAL_PENDING;
        }
        else if (targetType == ReversalTarget.COMPLETION) {
            if (this.state != PaymentState.COMPLETED) {
                throw new IllegalStateException("Cannot reverse COMPLETION if not COMPLETED");
            }
            this.state = PaymentState.COMPLETION_REVERSAL_PENDING;
        }
    }

    public String getId() { return id; }
    public PaymentState getState() { return state; }
    public String getTransRef() { return transRef; }
    public Long getVersion() { return version; }
    public BigDecimal getCompletedAmount() { return completedAmount; }
    public void confirmTransRef(String transRef) { this.transRef = transRef; }
    public void advanceState(PaymentState newState) { this.state = newState; }
}