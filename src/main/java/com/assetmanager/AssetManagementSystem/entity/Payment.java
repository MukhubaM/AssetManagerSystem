package com.assetmanager.AssetManagementSystem.entity;

import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// This is a mock payment record, this application does not integrate with a real payment processor
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loan_id", nullable = false, unique = true)
    private Loan loan;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime initiatedAt;

    private LocalDateTime confirmedAt;
}