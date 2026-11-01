package com.ptskart.api.repository;

import com.ptskart.api.entity.EmailConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailConfigRepository extends JpaRepository<EmailConfig, Long> {
    Optional<EmailConfig> findFirstByOrderByIdDesc();
    Optional<EmailConfig> findFirstByActiveTrueOrderByIdDesc();
    Optional<EmailConfig> findByProviderKey(String providerKey);
    boolean existsByProviderKey(String providerKey);
    List<EmailConfig> findAllByOrderByIdAsc();
}
