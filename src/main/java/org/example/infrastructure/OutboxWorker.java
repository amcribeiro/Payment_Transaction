package org.example.infrastructure;

import org.example.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxWorker {

    private final OutboxRepository outboxRepository;
    private final PaymentRepository paymentRepository;
    private final LedgerRepository ledgerRepository;
    private final OperationRepository operationRepository;
    private final PaymentHistoryRepository historyRepository;
    private final MockGateway gateway;

    public OutboxWorker(OutboxRepository outboxRepository,
                        PaymentRepository paymentRepository,
                        LedgerRepository ledgerRepository,
                        OperationRepository operationRepository,
                        PaymentHistoryRepository historyRepository,
                        MockGateway gateway) {
        this.outboxRepository = outboxRepository;
        this.paymentRepository = paymentRepository;
        this.ledgerRepository = ledgerRepository;
        this.operationRepository = operationRepository;
        this.historyRepository = historyRepository;
        this.gateway = gateway;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> events = outboxRepository.findByStatus("NEW");

        for (OutboxEvent event : events) {
            if (event.getNextAttemptAt() != null && event.getNextAttemptAt().isAfter(Instant.now())) {
                continue;
            }

            try {
                Payment payment = paymentRepository.findById(event.getPaymentId()).orElse(null);
                Operation op = operationRepository.findById(event.getOperationId()).orElse(null);


                if (payment != null && op != null) {
                    String oldState = payment.getState().name();

                    String gatewayRef = gateway.process();
                    payment.confirmTransRef(gatewayRef);

                    switch (op.getType()) {
                        case PREAUTH -> payment.advanceState(PaymentState.PREAUTH_AUTHORIZED);
                        case TOPUP -> {
                            payment.addTopupAmount(op.getAmount());
                            payment.advanceState(PaymentState.TOPUP_AUTHORIZED);
                        }
                        case COMPLETION -> payment.advanceState(PaymentState.COMPLETED);
                        case SALE -> payment.advanceState(PaymentState.SALE_COMPLETED);
                        case REVERSAL -> {
                            // Usa o target definido na operação (Ponto 7 das notas)
                            if (op.getTarget() == ReversalTarget.COMPLETION) {
                                payment.advanceState(PaymentState.COMPLETION_REVERSED);
                            } else {
                                payment.advanceState(PaymentState.PREAUTH_REVERSED);
                            }
                        }
                    }

                    historyRepository.save(new PaymentHistoryEntry(payment.getId(), oldState, payment.getState().name()));
                    ledgerRepository.save(new LedgerEntry(payment.getId(), op.getType().name(), op.getAmount()));

                    op.setStatus(OperationStatus.SUCCEEDED);

                    paymentRepository.save(payment);
                    operationRepository.save(op);

                    event.setStatus("DONE");
                } else {
                    event.setStatus("FAILED");
                }

                outboxRepository.save(event);

            } catch (PermanentGatewayException e) {
                event.setStatus("FAILED");
                outboxRepository.save(event);

            } catch (Exception e) {

                event.setRetryCount(event.getRetryCount() + 1);

                if (event.getRetryCount() > 3) {
                    event.setStatus("FAILED");

                } else {
                    event.setStatus("NEW");
                    event.setNextAttemptAt(Instant.now().plusSeconds(30));
                }
                outboxRepository.save(event);
            }
        }
    }
}