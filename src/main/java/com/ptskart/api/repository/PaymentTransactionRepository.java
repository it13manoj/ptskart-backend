package com.ptskart.api.repository;

import com.ptskart.api.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findAllByOrderByCreatedAtDesc();
    List<PaymentTransaction> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
    List<PaymentTransaction> findByGatewayKeyOrderByCreatedAtDesc(String gatewayKey);
    List<PaymentTransaction> findByStatusOrderByCreatedAtDesc(String status);
    Optional<PaymentTransaction> findByTransactionId(String transactionId);
    Optional<PaymentTransaction> findByOrderNumber(String orderNumber);
}

