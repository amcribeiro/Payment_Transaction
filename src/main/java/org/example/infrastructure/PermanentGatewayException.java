package org.example.infrastructure;

/**
 * Erros fatais (ex: Cartão Roubado, Conta Bloqueada, 403 Forbidden).
 * O Worker NÃO deve tentar novamente.
 */
public class PermanentGatewayException extends RuntimeException {
    public PermanentGatewayException(String message) {
        super(message);
    }
}