package org.example.infrastructure;

import org.example.domain.PaymentHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistoryEntry, String> {
    List<PaymentHistoryEntry> findByPaymentId(String paymentId);
}