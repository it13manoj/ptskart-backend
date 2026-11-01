package com.ptskart.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketNumber; // e.g. #TK-9041

    private String customerName;

    private String customerEmail;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, URGENT

    private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    private String assignedAgent = "Alex Chen";

    private LocalDateTime createdAt = LocalDateTime.now();

    public SupportTicket() {}

    public SupportTicket(String ticketNumber, String customerName, String customerEmail, String subject, String description, String priority, String status) {
        this.ticketNumber = ticketNumber;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedAgent() { return assignedAgent; }
    public void setAssignedAgent(String assignedAgent) { this.assignedAgent = assignedAgent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

