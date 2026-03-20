package org.example.api;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record OperationWithRefRequest(
        @NotNull(message = "amount é obrigatório")
        @Positive(message = "amount tem de ser maior que zero") BigDecimal amount,
        @NotBlank(message = "transRef é obrigatório para esta operação") String transRef
) {}