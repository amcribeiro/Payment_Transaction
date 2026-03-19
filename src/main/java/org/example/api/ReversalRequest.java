package org.example.api;

import org.example.domain.ReversalTarget;

public record ReversalRequest(ReversalTarget target, String reason) {}