package org.example.api;

import org.example.domain.Payment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldEnforceIdempotencyOnPaymentCreation() {
        String idemKey = "test-key-unique";
        CreatePaymentRequest req = new CreatePaymentRequest("m1", "o1", "EUR", null);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", idemKey);
        HttpEntity<CreatePaymentRequest> entity = new HttpEntity<>(req, headers);


        ResponseEntity<Payment> resp1 = restTemplate.postForEntity("/payments", entity, Payment.class);
        assertEquals(HttpStatus.CREATED, resp1.getStatusCode());
        String firstId = resp1.getBody().getId();


        ResponseEntity<Payment> resp2 = restTemplate.postForEntity("/payments", entity, Payment.class);
        assertEquals(HttpStatus.CREATED, resp2.getStatusCode());
        assertEquals(firstId, resp2.getBody().getId());


        CreatePaymentRequest diffReq = new CreatePaymentRequest("m2", "o2", "USD", null);
        HttpEntity<CreatePaymentRequest> diffEntity = new HttpEntity<>(diffReq, headers);
        ResponseEntity<String> resp3 = restTemplate.postForEntity("/payments", diffEntity, String.class);
        assertEquals(HttpStatus.CONFLICT, resp3.getStatusCode());
    }
}