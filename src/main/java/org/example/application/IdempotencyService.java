package org.example.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.infrastructure.IdempotencyRecord;
import org.example.infrastructure.IdempotencyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.function.Supplier;

@Service
public class IdempotencyService {

    private final IdempotencyRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ResponseEntity<?> executeIdempotent(String idempotencyKey, String endpoint, Object requestBody, Supplier<ResponseEntity<?>> businessLogic) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required for this operation");
        }

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            String hash = generateHash(jsonBody + "|" + endpoint);

            IdempotencyRecord existing = repository.findById(idempotencyKey).orElse(null);

            if (existing != null) {
                if (existing.getRequestHash().equals(hash)) {
                    Object body = objectMapper.readTree(existing.getResponseBody());
                    return ResponseEntity.status(existing.getResponseStatus()).body(body);
                } else {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("{\"error\": \"IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD\"}");
                }
            }

            IdempotencyRecord record = new IdempotencyRecord(idempotencyKey, endpoint, hash);
            repository.saveAndFlush(record);

            ResponseEntity<?> response = businessLogic.get();

            record.updateResponse(response.getStatusCode().value(), objectMapper.writeValueAsString(response.getBody()));
            repository.save(record);

            return response;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao processar JSON para idempotência", e);
        }
    }

    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash SHA-256", e);
        }
    }
}