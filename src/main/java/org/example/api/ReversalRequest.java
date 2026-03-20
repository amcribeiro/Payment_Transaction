package org.example.api;

import org.example.domain.ReversalTarget;
import jakarta.validation.constraints.*;

public record ReversalRequest(
        @NotNull(message = "target is required") ReversalTarget target,
        @NotBlank(message = "reason is required") String reason
) {}