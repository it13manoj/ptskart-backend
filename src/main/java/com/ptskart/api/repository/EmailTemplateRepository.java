package com.ptskart.api.repository;

import com.ptskart.api.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByTemplateKey(String templateKey);
    boolean existsByTemplateKey(String templateKey);
    List<EmailTemplate> findAllByOrderByIdAsc();
}

