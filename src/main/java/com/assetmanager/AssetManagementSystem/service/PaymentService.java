package com.assetmanager.AssetManagementSystem.service;

import com.assetmanager.AssetManagementSystem.entity.Payment;

import java.util.Optional;

// A mock payment workflow
public interface PaymentService {

    // Creates a PENDING payment for an APPROVED loan, with the amount snap shotted from the loan's calculated total cost
    Payment initiatePayment(Long loanId, String actorEmail);


    // Marks a PENDING payment COMPLETED.
    Payment confirmPayment(Long paymentId, String actorEmail);


    // Deletes the payment record outright
    void rollbackPayment(Long paymentId, String actorEmail);


    Payment getPayment(Long paymentId);


    Optional<Payment> getPaymentForLoan(Long loanId);
}