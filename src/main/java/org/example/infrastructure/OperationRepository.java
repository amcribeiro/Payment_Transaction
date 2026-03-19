package org.example.infrastructure;

import org.example.domain.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OperationRepository extends JpaRepository<Operation, String> {
    List<Operation> findByPaymentId(String paymentId);
}