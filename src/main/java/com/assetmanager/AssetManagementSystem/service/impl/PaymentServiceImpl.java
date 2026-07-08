package com.assetmanager.AssetManagementSystem.service.impl;

import com.assetmanager.AssetManagementSystem.entity.*;
import com.assetmanager.AssetManagementSystem.exception.BusinessRuleException;
import com.assetmanager.AssetManagementSystem.exception.ResourceNotFoundException;
import com.assetmanager.AssetManagementSystem.repository.LoanRepository;
import com.assetmanager.AssetManagementSystem.repository.PaymentRepository;
import com.assetmanager.AssetManagementSystem.repository.UserRepository;
import com.assetmanager.AssetManagementSystem.security.CurrentUserProvider;
import com.assetmanager.AssetManagementSystem.service.AuditLogService;
import com.assetmanager.AssetManagementSystem.service.PaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final LoanRepository loanRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public Payment initiatePayment(Long loanId, String actorEmail) {

        Loan loan = fetchLoan(loanId);
        User actor = fetchUser(actorEmail);

        requireBorrowerOrStaff(loan, actor);

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new BusinessRuleException("Payment can only be initiated for an approved loan");
        }

        if (paymentRepository.findByLoan(loan).isPresent()) {
            throw new BusinessRuleException("A payment already exists for this loan");
        }

        if (loan.getTotalCost() == null) {
            throw new BusinessRuleException("This loan has no calculated cost to pay");
        }

        Payment payment = Payment.builder()
                .loan(loan)
                .amount(loan.getTotalCost())
                .status(PaymentStatus.PENDING)
                .initiatedAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "PAYMENT", savedPayment.getPaymentId(), "INITIATE");

        return savedPayment;
    }

    @Override
    @Transactional
    public Payment confirmPayment(Long paymentId, String actorEmail) {

        Payment payment = fetchPayment(paymentId);
        User actor = fetchUser(actorEmail);

        requireBorrowerOrStaff(payment.getLoan(), actor);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessRuleException("Only pending payment can be confirmed");
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setConfirmedAt(LocalDateTime.now());

        Payment confirmedPayment = paymentRepository.save(payment);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "PAYMENT", confirmedPayment.getPaymentId(), "CONFIRM");

        return confirmedPayment;
    }

    @Override
    @Transactional
    public void rollbackPayment(Long paymentId, String actorEmail) {

        Payment payment = fetchPayment(paymentId);
        User actor = fetchUser(actorEmail);

        Loan loan = payment.getLoan();

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new BusinessRuleException("This loan has already been checked out - payment can no longer be rolled back");
        }

        // A completed (already-paid) transaction is more sensitive to reverse than one that's still pending, so only admin can roll it back
        // For borrower to be able to also cancel their own pending payment
        boolean isStaff = actor.getRole() == Role.ADMIN || actor.getRole() == Role.MANAGER;

        if (payment.getStatus() == PaymentStatus.COMPLETED && !isStaff) {
            throw new BusinessRuleException("Only a manager or admin can roll back a completed payment");
        }

        if (payment.getStatus() == PaymentStatus.PENDING) {
            requireBorrowerOrStaff(loan, actor);
        }

        // Rollback simplified to deleting the transaction record outright,this is a mock payment system with no real money movement to reverse
        paymentRepository.delete(payment);

        auditLogService.log(currentUserProvider.getCurrentUserId(), "PAYMENT", paymentId, "ROLLBACK");
    }

    @Override
    public Payment getPayment(Long paymentId) {

        return fetchPayment(paymentId);
    }

    @Override
    public Optional<Payment> getPaymentForLoan(Long loanId) {

        Loan loan = fetchLoan(loanId);

        return paymentRepository.findByLoan(loan);
    }

    private void requireBorrowerOrStaff(Loan loan, User actor) {

        boolean isOwner = loan.getBorrower().getUserId().equals(actor.getUserId());
        boolean isStaff = actor.getRole() == Role.ADMIN || actor.getRole() == Role.MANAGER;

        if (!isOwner && !isStaff) {
            throw new BusinessRuleException("You do not have permission to manage payment for this loan");
        }
    }

    private Loan fetchLoan(Long id) {

        return loanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
    }

    private Payment fetchPayment(Long id) {

        return paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    private User fetchUser(String email) {

        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}