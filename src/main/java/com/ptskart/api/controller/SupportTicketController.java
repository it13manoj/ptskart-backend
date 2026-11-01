package com.ptskart.api.controller;

import com.ptskart.api.dto.ApiResponse;
import com.ptskart.api.entity.SupportTicket;
import com.ptskart.api.repository.SupportTicketRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/tickets")
public class SupportTicketController {

    private final SupportTicketRepository ticketRepository;

    public SupportTicketController(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupportTicket>>> getAllTickets() {
        return ResponseEntity.ok(ApiResponse.ok("Support tickets retrieved", ticketRepository.findAllByOrderByCreatedAtDesc()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupportTicket>> createTicket(@RequestBody SupportTicket ticket) {
        if (ticket.getTicketNumber() == null || ticket.getTicketNumber().isBlank()) {
            ticket.setTicketNumber("#TK-" + (1000 + new Random().nextInt(9000)));
        }
        if (ticket.getCreatedAt() == null) {
            ticket.setCreatedAt(LocalDateTime.now());
        }
        return ResponseEntity.ok(ApiResponse.ok("Ticket opened successfully", ticketRepository.save(ticket)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SupportTicket>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ticketRepository.findById(id).map(t -> {
            t.setStatus(status);
            return ResponseEntity.ok(ApiResponse.ok("Ticket status changed to " + status, ticketRepository.save(t)));
        }).orElse(ResponseEntity.notFound().build());
    }
}

