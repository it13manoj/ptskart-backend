package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.entity.CustomerMessage;
import com.ptskart.api.repository.CustomerMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class CustomerMessageController {

    private final CustomerMessageRepository messageRepository;

    public CustomerMessageController(CustomerMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerMessage>>> getAllMessages() {
        return ResponseEntity.ok(ApiResponse.ok("Customer messages retrieved", messageRepository.findAllByOrderByReceivedAtDesc()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerMessage>> sendMessage(@RequestBody CustomerMessage message) {
        if (message.getReceivedAt() == null) {
            message.setReceivedAt(LocalDateTime.now());
        }
        return ResponseEntity.ok(ApiResponse.ok("Message received. Our team will follow up promptly.", messageRepository.save(message)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<CustomerMessage>> markRead(@PathVariable Long id) {
        return messageRepository.findById(id).map(m -> {
            m.setIsRead(true);
            return ResponseEntity.ok(ApiResponse.ok("Message marked as read", messageRepository.save(m)));
        }).orElse(ResponseEntity.notFound().build());
    }
}

