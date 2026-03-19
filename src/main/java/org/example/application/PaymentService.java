package org.example.application;

import org.example.api.OperationResponse;
import org.example.domain.*;
import org.example.infrastructure.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxRepository outboxRepository;
    private final OperationRepository operationRepository;
    private final LedgerRepository ledgerRepository;
    private final PaymentHistoryRepository historyRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          OutboxRepository outboxRepository,
                          OperationRepository operationRepository,
                          LedgerRepository ledgerRepository,
                          PaymentHistoryRepository historyRepository) {
        this.paymentRepository = paymentRepository;
        this.outboxRepository = outboxRepository;
        this.operationRepository = operationRepository;
        this.ledgerRepository = ledgerRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public Payment createPayment(String merchantId, String orderId, String currency, BigDecimal amount) {
        Payment payment = new Payment(merchantId, orderId, currency, amount);
        return paymentRepository.save(payment);
    }

    @Transactional
    public OperationResponse requestPreauth(String paymentId, BigDecimal amount) {
        Payment payment = getPayment(paymentId);
        payment.requestPreauth(amount);
        paymentRepository.save(payment);

        Operation op = new Operation(paymentId, OperationType.PREAUTH, amount, null, null, null);
        operationRepository.save(op);

        outboxRepository.save(new OutboxEvent(paymentId, op.getId(), "PREAUTH_REQUESTED"));
        return new OperationResponse(op.getId(), paymentId, "PREAUTH", "PENDING", amount, null);
    }

    @Transactional
    public OperationResponse requestCompletion(String paymentId, BigDecimal amount, String transRef) {
        Payment payment = getPayment(paymentId);
        payment.requestCompletion(amount, transRef);
        paymentRepository.save(payment);

        Operation op = new Operation(paymentId, OperationType.COMPLETION, amount, transRef, null, null);
        operationRepository.save(op);

        outboxRepository.save(new OutboxEvent(paymentId, op.getId(), "COMPLETION_REQUESTED"));
        return new OperationResponse(op.getId(), paymentId, "COMPLETION", "PENDING", amount, transRef);
    }

    @Transactional
    public OperationResponse requestTopup(String paymentId, BigDecimal amount, String transRef) {
        Payment payment = getPayment(paymentId);
        payment.requestTopup(amount, transRef);
        paymentRepository.save(payment);

        Operation op = new Operation(paymentId, OperationType.TOPUP, amount, transRef, null, null);
        operationRepository.save(op);

        outboxRepository.save(new OutboxEvent(paymentId, op.getId(), "TOPUP_REQUESTED"));
        return new OperationResponse(op.getId(), paymentId, "TOPUP", "PENDING", amount, transRef);
    }

    @Transactional
    public OperationResponse requestSale(String paymentId, BigDecimal amount) {
        Payment payment = getPayment(paymentId);
        payment.requestSale(amount);
        paymentRepository.save(payment);

        Operation op = new Operation(paymentId, OperationType.SALE, amount, null, null, null);
        operationRepository.save(op);

        outboxRepository.save(new OutboxEvent(paymentId, op.getId(), "SALE_REQUESTED"));
        return new OperationResponse(op.getId(), paymentId, "SALE", "PENDING", amount, null);
    }

    @Transactional
    public OperationResponse requestReversal(String paymentId, ReversalTarget target, String reason) {
        Payment payment = getPayment(paymentId);
        payment.requestReversal(target);
        paymentRepository.save(payment);

        Operation op = new Operation(paymentId, OperationType.REVERSAL, BigDecimal.ZERO, null, target, reason);
        operationRepository.save(op);

        outboxRepository.save(new OutboxEvent(paymentId, op.getId(), target + "_REVERSAL_REQUESTED"));
        return new OperationResponse(op.getId(), paymentId, "REVERSAL", "PENDING", BigDecimal.ZERO, null);
    }

    public Payment getPayment(String id) {
        return paymentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }

    public List<Operation> getOperationsByPaymentId(String paymentId) {
        return operationRepository.findByPaymentId(paymentId);
    }

    public List<PaymentHistoryEntry> getHistoryByPaymentId(String paymentId) {
        return historyRepository.findByPaymentId(paymentId);
    }

    public List<LedgerEntry> getLedgerByPaymentId(String paymentId) {
        return ledgerRepository.findByPaymentId(paymentId);
    }

    public List<OutboxEvent> getTechnicalHistoryByPaymentId(String paymentId) {
        return outboxRepository.findByPaymentId(paymentId);
    }
}