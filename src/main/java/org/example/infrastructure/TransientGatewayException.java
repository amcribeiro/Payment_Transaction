package org.example.infrastructure;

/**
 * Erros temporários (ex: Timeout, 503 Service Unavailable).
 * O Worker DEVE tentar novamente.
 */
public class TransientGatewayException extends RuntimeException {
    public TransientGatewayException(String message) {
        super(message);
    }
}