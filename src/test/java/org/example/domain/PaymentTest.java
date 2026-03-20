package org.example.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @Test
    void shouldRejectCompletionWhenAmountExceedsAuthorizedTotal() {

        Payment payment = new Payment("m1", "o1", "EUR", new BigDecimal("100.00"));

        payment.requestPreauth(new BigDecimal("100.00"));
        payment.confirmTransRef("A1B2C3");
        payment.advanceState(PaymentState.PREAUTH_AUTHORIZED);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            payment.requestCompletion(new BigDecimal("120.00"), "A1B2C3");
        });

        assertEquals("INSUFFICIENT_AUTHORIZED_TOTAL", exception.getMessage());
    }

    @Test
    void shouldAllowCompletionAfterTopup() {
        Payment payment = new Payment("m1", "o1", "EUR", new BigDecimal("100.00"));

        payment.requestPreauth(new BigDecimal("100.00"));
        payment.confirmTransRef("A1B2C3");
        payment.advanceState(PaymentState.PREAUTH_AUTHORIZED);

        payment.requestTopup(new BigDecimal("30.00"), "A1B2C3");

        payment.addTopupAmount(new BigDecimal("30.00"));
        payment.advanceState(PaymentState.TOPUP_AUTHORIZED);

        assertDoesNotThrow(() -> payment.requestCompletion(new BigDecimal("120.00"), "A1B2C3"));
    }
    @Test
    void shouldRejectPreauthReversalWhenAlreadyCompleted() {
        Payment payment = new Payment("m1", "o1", "EUR", new BigDecimal("100.00"));

        payment.requestPreauth(new BigDecimal("100.00"));

        String realRef = "GATEWAY_ABC_123";
        payment.confirmTransRef(realRef);
        payment.advanceState(PaymentState.PREAUTH_AUTHORIZED);

        payment.requestCompletion(new BigDecimal("100.00"), realRef);
        payment.advanceState(PaymentState.COMPLETED);

        assertThrows(IllegalStateException.class, () -> payment.requestReversal(ReversalTarget.PREAUTH));
    }

    @Test
    void shouldRejectCompletionWhenTransRefMismatch() {
        Payment payment = new Payment("m1", "o1", "EUR", new BigDecimal("100.00"));
        payment.requestPreauth(new BigDecimal("100.00"));
        payment.confirmTransRef("ORIGINAL_REF");
        payment.advanceState(PaymentState.PREAUTH_AUTHORIZED);

        assertThrows(IllegalArgumentException.class, () ->
                payment.requestCompletion(new BigDecimal("100.00"), "WRONG_REF")
        );
    }
}