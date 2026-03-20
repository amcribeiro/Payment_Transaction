package org.example.api;

import org.example.domain.Payment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PaymentApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldEnforceIdempotencyOnPaymentCreation() {

        String idemKey = UUID.randomUUID().toString();
        BigDecimal amount = new BigDecimal("100.00");
        CreatePaymentRequest req = new CreatePaymentRequest("m1", "o1", "EUR", amount);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", idemKey);
        HttpEntity<CreatePaymentRequest> entity = new HttpEntity<>(req, headers);

        ResponseEntity<Payment> resp1 = restTemplate.postForEntity("/payments", entity, Payment.class);
        assertEquals(HttpStatus.CREATED, resp1.getStatusCode());
        assertNotNull(resp1.getBody());
        String firstId = resp1.getBody().getId();

        ResponseEntity<Payment> resp2 = restTemplate.postForEntity("/payments", entity, Payment.class);
        assertEquals(HttpStatus.CREATED, resp2.getStatusCode());
        assertEquals(firstId, resp2.getBody().getId());

        BigDecimal differentAmount = new BigDecimal("200.00");
        CreatePaymentRequest diffReq = new CreatePaymentRequest("m1", "o1", "EUR", differentAmount);
        HttpEntity<CreatePaymentRequest> diffEntity = new HttpEntity<>(diffReq, headers);

        ResponseEntity<String> resp3 = restTemplate.postForEntity("/payments", diffEntity, String.class);
        assertEquals(HttpStatus.CONFLICT, resp3.getStatusCode());
        assertTrue(resp3.getBody().contains("IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD"));
    }
}