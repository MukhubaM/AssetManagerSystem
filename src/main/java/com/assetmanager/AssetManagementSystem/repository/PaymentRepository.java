package com.assetmanager.AssetManagementSystem.repository;

import com.assetmanager.AssetManagementSystem.entity.Loan;
import com.assetmanager.AssetManagementSystem.entity.Payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByLoan(Loan loan);
}