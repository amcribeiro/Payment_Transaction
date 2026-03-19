package org.example.api;

import java.math.BigDecimal;

public record OperationResponse(String operationId, String paymentId, String type, String status, BigDecimal amount, String transRef) {}
