package org.example.api;
import java.math.BigDecimal;

public record OperationRequest(BigDecimal amount, String transRef) {}