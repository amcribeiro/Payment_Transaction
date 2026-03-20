package org.example.infrastructure;

import org.example.domain.OperationType;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class MockGateway {

    public String process(OperationType type, java.math.BigDecimal amount) {
        double chance = Math.random();

        double timeoutChance = (type == OperationType.REVERSAL) ? 0.05 : 0.15;
        if (chance < timeoutChance) {
            throw new TransientGatewayException("GATEWAY_TIMEOUT na operação " + type);
        }

        if (chance < 0.20) {
            throw new PermanentGatewayException("HARD_REJECTION: Fraude ou Cartão Inválido");
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}