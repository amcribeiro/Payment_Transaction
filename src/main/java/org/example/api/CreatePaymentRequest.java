package org.example.api;
import java.math.BigDecimal;

public record CreatePaymentRequest(String merchantId, String orderId, String currency, BigDecimal amount) {}