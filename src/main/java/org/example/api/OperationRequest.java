package org.example.api;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record OperationRequest(
        @NotNull(message = "amount é obrigatório")
        @Positive(message = "amount tem de ser maior que zero") BigDecimal amount
) {}