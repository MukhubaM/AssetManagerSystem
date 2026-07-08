package com.assetmanager.AssetManagementSystem.service.impl;

import com.assetmanager.AssetManagementSystem.entity.*;
import com.assetmanager.AssetManagementSystem.exception.BusinessRuleException;
import com.assetmanager.AssetManagementSystem.exception.ResourceNotFoundException;
import com.assetmanager.AssetManagementSystem.repository.AssetRepository;
import com.assetmanager.AssetManagementSystem.repository.LoanRepository;
import com.assetmanager.AssetManagementSystem.repository.PaymentRepository;
import com.assetmanager.AssetManagementSystem.repository.UserRepository;
import com.assetmanager.AssetManagementSystem.security.CurrentUserProvider;
import com.assetmanager.AssetManagementSystem.service.AuditLogService;
import com.assetmanager.AssetManagementSystem.service.LoanService;
import com.assetmanager.AssetManagementSystem.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationService notificationService;

    private static final int MAX_LOANS = 3;
    private static final int FALLBACK_LOAN_PERIOD_DAYS = 14;
    private static final int DUE_SOON_WINDOW_DAYS = 2;        // 48 hours, at day-level

    @Override
    @Transactional
    public Loan requestLoan(Long assetId, String email, Integer durationDays) {

        User borrower = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        Asset asset = assetRepository.findById(assetId).orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));

        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new BusinessRuleException("Asset is not available for loan");
        }

        if (asset.getDailyRate() == null) {
            throw new BusinessRuleException("This asset doesn't have a daily rate set yet - ask a manager to update it before requesting a loan");
        }

        if (durationDays == null || durationDays < 1) {
            throw new BusinessRuleException("Please choose a valid loan duration");
        }

        long activeLoans = loanRepository.countByBorrowerAndStatus(borrower, LoanStatus.CHECKED_OUT);

        if (activeLoans >= MAX_LOANS) {
            throw new BusinessRuleException("Maximum number of active loans (" + MAX_LOANS + ") reached");
        }

        BigDecimal totalCost = asset.getDailyRate().multiply(BigDecimal.valueOf(durationDays));

        Loan loan = Loan.builder()
                .asset(asset)
                .borrower(borrower)
                .requestDate(LocalDate.now())
                .status(LoanStatus.PENDING)
                .durationDays(durationDays)
                .totalCost(totalCost)
                .build();

        Loan savedLoan = loanRepository.save(loan);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "LOAN", savedLoan.getLoanId(), "REQUEST");

        notificationService.notify(borrower,
                NotificationType.LOAN_REQUESTED,
                    "Loan request submitted",
                 "Your request to borrow \"" + asset.getTitle() + "\" for " + durationDays + " day(s) "
                        + "(estimated cost: " + totalCost + ") has been submitted and is awaiting approval.");

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

        notificationService.notify(approvedLoan.getBorrower(),
                NotificationType.LOAN_APPROVED,
                     "Loan approved",
                 "Your request to borrow \"" + approvedLoan.getAsset().getTitle() + "\" was approved. "
                        + "Total cost: " + approvedLoan.getTotalCost() + ". Please complete payment to have it checked out.");

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

        notificationService.notify(rejectedLoan.getBorrower(), NotificationType.LOAN_REJECTED,
                   "Loan request rejected",
                "Your request to borrow \"" + rejectedLoan.getAsset().getTitle() + "\" was not approved.");

        return rejectedLoan;
    }

    @Override
    @Transactional
    public Loan checkOut(Long loanId) {

        Loan loan = getLoan(loanId);

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new BusinessRuleException("Loan must be approved before it can be checked out");
        }

        Payment payment = paymentRepository.findByLoan(loan).orElseThrow(() -> new BusinessRuleException("This loan has not been paid for yet"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessRuleException("Payment must be completed before this loan can be checked out");
        }

        int durationDays = loan.getDurationDays() != null ? loan.getDurationDays() : FALLBACK_LOAN_PERIOD_DAYS;

        loan.setCheckoutDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(durationDays));
        loan.setStatus(LoanStatus.CHECKED_OUT);

        Asset asset = loan.getAsset();
        asset.setStatus(AssetStatus.LOANED);
        assetRepository.save(asset);

        Loan checkedOutLoan = loanRepository.save(loan);
        auditLogService.log(currentUserProvider.getCurrentUserId(), "LOAN", checkedOutLoan.getLoanId(), "CHECK_OUT");

        notificationService.notify(checkedOutLoan.getBorrower(), NotificationType.LOAN_CHECKED_OUT,
                   "Asset checked out",
                "You've checked out \"" + asset.getTitle() + "\". It's due back on " + checkedOutLoan.getDueDate() + ".");

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

        notificationService.notify(returnedLoan.getBorrower(), NotificationType.LOAN_RETURNED,
                   "Asset returned",
                "Thanks for returning \"" + asset.getTitle() + "\". Your loan is now closed.");

        return returnedLoan;
    }

    @Override
    public List<Loan> getAllLoans() {

        return loanRepository.findAll();
    }

    @Override
    public List<Loan> getUserLoans(String email) {

        User borrower = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        return loanRepository.findByBorrower(borrower);
    }

    @Override
    public List<Loan> getOverdueLoans() {

        return loanRepository.findByStatus(LoanStatus.OVERDUE);
    }

    private Loan getLoan(Long id) {

        return loanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
    }

    // Flags checked-out loans when due date has passed as OVERDUE
    @Override
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void updateOverdueLoans() {

        List<Loan> checkedOutLoans = loanRepository.findByStatus(LoanStatus.CHECKED_OUT);

        for (Loan loan : checkedOutLoans) {

            if (loan.getDueDate() != null && loan.getDueDate().isBefore(LocalDate.now())) {

                loan.setStatus(LoanStatus.OVERDUE);
                loanRepository.save(loan);

                auditLogService.log(currentUserProvider.getCurrentUserId(), "LOAN", loan.getLoanId(), "OVERDUE");
                log.info("Loan {} marked OVERDUE (due {})", loan.getLoanId(), loan.getDueDate());

                notificationService.notify(loan.getBorrower(), NotificationType.LOAN_OVERDUE,
                            "Loan overdue",
                         "\"" + loan.getAsset().getTitle() + "\" was due on " + loan.getDueDate()
                                + " and is now overdue. Please return it as soon as possible.");
            }
        }
    }

    // Warns borrowers when checked-out loan is due within the next 48hours
    @Override
    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void sendDueSoonWarnings() {

        List<Loan> candidates = loanRepository.findByStatusAndDueSoonNotifiedFalse(LoanStatus.CHECKED_OUT);

        LocalDate today = LocalDate.now();
        LocalDate warnWindowEnd = today.plusDays(DUE_SOON_WINDOW_DAYS);

        for (Loan loan : candidates) {

            LocalDate dueDate = loan.getDueDate();

            if (dueDate != null && !dueDate.isBefore(today) && !dueDate.isAfter(warnWindowEnd)) {

                loan.setDueSoonNotified(true);
                loanRepository.save(loan);

                notificationService.notify(loan.getBorrower(), NotificationType.LOAN_DUE_SOON,
                            "Loan due soon",
                         "\"" + loan.getAsset().getTitle() + "\" is due back on " + dueDate
                                + " - please plan to return it soon.");

                log.info("Sent due-soon warning for loan {} (due {})", loan.getLoanId(), dueDate);
            }
        }
    }
}