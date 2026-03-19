package org.example.infrastructure;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class MockGateway {

    public String process() {
        double chance = Math.random();

        if (chance < 0.15) {
            throw new TransientGatewayException("GATEWAY_TIMEOUT - Connection lost");
        }

        if (chance < 0.20) {
            throw new PermanentGatewayException("HARD_GATEWAY_REJECTION - Fraud or Invalid Card");
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}