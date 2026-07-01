package com.assetmanager.AssetManagementSystem.service.impl;

import com.assetmanager.AssetManagementSystem.entity.*;
import com.assetmanager.AssetManagementSystem.exception.BusinessRuleException;
import com.assetmanager.AssetManagementSystem.exception.ResourceNotFoundException;
import com.assetmanager.AssetManagementSystem.repository.AssetRepository;
import com.assetmanager.AssetManagementSystem.repository.LoanRepository;
import com.assetmanager.AssetManagementSystem.repository.UserRepository;
import com.assetmanager.AssetManagementSystem.security.CurrentUserProvider;
import com.assetmanager.AssetManagementSystem.service.AuditLogService;
import com.assetmanager.AssetManagementSystem.service.LoanService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;

    private static final int MAX_LOANS = 3;
    private static final int LOAN_PERIOD_DAYS = 14;

    @Override
    @Transactional
    public Loan requestLoan(Long assetId, String email) {

        User borrower = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));

        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new BusinessRuleException("Asset not available for loan");
        }

        long activeLoans = loanRepository.countByBorrowerAndStatus(borrower, LoanStatus.CHECKED_OUT);

        if (activeLoans >= MAX_LOANS) {
            throw new BusinessRuleException("Maximum number of active loans (" + MAX_LOANS + ") reached");
        }

        Loan loan = Loan.builder()
                .asset(asset)
                .borrower(borrower)
                .requestDate(LocalDate.now())
                .status(LoanStatus.PENDING)
                .build();

        Loan savedLoan = loanRepository.save(loan);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "LOAN", savedLoan.getLoanId(), "REQUEST");

        return savedLoan;
    }

    @Override
    @Transactional
    public Loan approveLoan(Long loanId) {

        Loan loan = getLoan(loanId);

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessRuleException("Only pending loans can be approved");
        }

        loan.setStatus(LoanStatus.APPROVED);

        Loan approvedLoan = loanRepository.save(loan);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "LOAN", approvedLoan.getLoanId(), "APPROVE");

        return approvedLoan;
    }

    @Override
    @Transactional
    public Loan rejectLoan(Long loanId) {

        Loan loan = getLoan(loanId);

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessRuleException("Only pending loans can be rejected");
        }

        loan.setStatus(LoanStatus.REJECTED);

        Loan rejectedLoan = loanRepository.save(loan);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "LOAN", rejectedLoan.getLoanId(), "REJECT");

        return rejectedLoan;
    }

    @Override
    @Transactional
    public Loan checkOut(Long loanId) {

        Loan loan = getLoan(loanId);

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new BusinessRuleException("Loan must be approved before it can be checked-out");
        }

        loan.setCheckoutDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(LOAN_PERIOD_DAYS));
        loan.setStatus(LoanStatus.CHECKED_OUT);

        Asset asset = loan.getAsset();
        asset.setStatus(AssetStatus.LOANED);
        assetRepository.save(asset);

        Loan checkedOutLoan = loanRepository.save(loan);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "LOAN", checkedOutLoan.getLoanId(), "CHECK_OUT");

        return checkedOutLoan;
    }

    @Override
    @Transactional
    public Loan returnAsset(Long loanId) {

        Loan loan = getLoan(loanId);

        if (loan.getStatus() != LoanStatus.CHECKED_OUT && loan.getStatus() != LoanStatus.OVERDUE) {
            throw new BusinessRuleException("Only checked-out or overdue loans can be returned");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        Asset asset = loan.getAsset();
        asset.setStatus(AssetStatus.AVAILABLE);
        assetRepository.save(asset);

        Loan returnedLoan = loanRepository.save(loan);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "LOAN", returnedLoan.getLoanId(), "RETURN");

        return returnedLoan;
    }

    @Override
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Override
    public List<Loan> getUserLoans(String email) {

        User borrower = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        return loanRepository.findByBorrower(borrower);
    }

    @Override
    public List<Loan> getOverdueLoans() {
        return loanRepository.findByStatus(LoanStatus.OVERDUE);
    }

    private Loan getLoan(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
    }

    // Flags checked-out loans when due date has passed, as OVERDUE
    // Runs once a day and also callable (directly)
    @Override
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")     // Run every day at exactly 1am
    public void updateOverdueLoans() {

        List<Loan> checkedOutLoans = loanRepository.findByStatus(LoanStatus.CHECKED_OUT);

        for (Loan loan : checkedOutLoans) {

            if (loan.getDueDate() != null && loan.getDueDate().isBefore(LocalDate.now())) {

                loan.setStatus(LoanStatus.OVERDUE);
                loanRepository.save(loan);

                auditLogService.log(currentUserProvider.getCurrentUserId(), "LOAN", loan.getLoanId(), "OVERDUE");
                log.info("Loan {} marked OVERDUE (due {})", loan.getLoanId(), loan.getDueDate());                                         // Log an informational message whenever a loan is automatically marked as overdue, {} placeholders are replaced with loan id and due date
            }
        }
    }
}
