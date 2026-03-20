package org.example.api;

import jakarta.validation.Valid;
import org.example.application.IdempotencyService;
import org.example.application.PaymentService;
import org.example.domain.Payment;
import org.example.domain.ReversalTarget;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;

    public PaymentController(PaymentService paymentService, IdempotencyService idempotencyService) {
        this.paymentService = paymentService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping
    public ResponseEntity<?> createPayment(
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request) {

        return idempotencyService.executeIdempotent(idempotencyKey, "/payments", request, () -> {
            Payment payment = paymentService.createPayment(
                    request.merchantId(), request.orderId(), request.currency(), request.amount());
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        });
    }

    @PostMapping("/{id}/preauth")
    public ResponseEntity<?> requestPreauth(
            @PathVariable("id") String paymentId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody OperationRequest request) {

        return idempotencyService.executeIdempotent(idempotencyKey, "/payments/" + paymentId + "/preauth", request, () -> {
            OperationResponse response = paymentService.requestPreauth(paymentId, request.amount());
            return ResponseEntity.accepted().body(response);
        });
    }

    @PostMapping("/{id}/sale")
    public ResponseEntity<?> requestSale(
            @PathVariable("id") String paymentId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody OperationRequest request) {

        return idempotencyService.executeIdempotent(
                idempotencyKey,
                "/payments/" + paymentId + "/sale",
                request,
                () -> {
                    OperationResponse response = paymentService.requestSale(paymentId, request.amount());
                    return ResponseEntity.accepted().body(response);
                }
        );
    }

    @PostMapping("/{id}/topup")
    public ResponseEntity<?> requestTopup(
            @PathVariable("id") String paymentId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody OperationWithRefRequest request) { // ALTERADO: Usa agora OperationWithRefRequest

        return idempotencyService.executeIdempotent(idempotencyKey, "/payments/" + paymentId + "/topup", request, () -> {
            OperationResponse response = paymentService.requestTopup(paymentId, request.amount(), request.transRef());
            return ResponseEntity.accepted().body(response);
        });
    }

    @PostMapping("/{id}/completion")
    public ResponseEntity<?> requestCompletion(
            @PathVariable("id") String paymentId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody OperationWithRefRequest request) {

        return idempotencyService.executeIdempotent(idempotencyKey, "/payments/" + paymentId + "/completion", request, () -> {
            OperationResponse response = paymentService.requestCompletion(paymentId, request.amount(), request.transRef());
            return ResponseEntity.accepted().body(response);
        });
    }

    @PostMapping("/{id}/reversal")
    public ResponseEntity<?> requestReversal(
            @PathVariable("id") String paymentId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ReversalRequest request) {

        return idempotencyService.executeIdempotent(idempotencyKey, "/payments/" + paymentId + "/reversal", request, () -> {
            OperationResponse response = paymentService.requestReversal(paymentId, request.target(), request.reason());
            return ResponseEntity.accepted().body(response);
        });
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping("/{id}/operations")
    public ResponseEntity<?> getPaymentOperations(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getOperationsByPaymentId(id));
    }

    @GetMapping("/{id}/ledger")
    public ResponseEntity<?> getPaymentLedger(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getLedgerByPaymentId(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<?> getPaymentHistory(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getHistoryByPaymentId(id));
    }
}