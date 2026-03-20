package org.example.api;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotBlank(message = "merchantId é obrigatório") String merchantId,
        @NotBlank(message = "orderId é obrigatório") String orderId,
        @NotBlank(message = "currency é obrigatório") String currency,
        @NotNull(message = "amount é obrigatório")
        @Positive(message = "amount tem de ser maior que zero") BigDecimal amount
) {}