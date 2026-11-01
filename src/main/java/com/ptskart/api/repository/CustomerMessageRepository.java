package com.ptskart.api.repository;

import com.ptskart.api.entity.CustomerMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerMessageRepository extends JpaRepository<CustomerMessage, Long> {
    List<CustomerMessage> findAllByOrderByReceivedAtDesc();
}

