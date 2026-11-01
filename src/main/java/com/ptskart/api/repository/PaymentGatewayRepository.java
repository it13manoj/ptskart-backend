package com.ptskart.api.repository;

import com.ptskart.api.entity.PaymentGatewayConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentGatewayRepository extends JpaRepository<PaymentGatewayConfig, Long> {
    Optional<PaymentGatewayConfig> findByGatewayKey(String gatewayKey);
    List<PaymentGatewayConfig> findByEnabledTrue();
}

