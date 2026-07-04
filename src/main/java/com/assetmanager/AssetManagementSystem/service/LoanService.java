package com.assetmanager.AssetManagementSystem.service;

import com.assetmanager.AssetManagementSystem.entity.Loan;

import java.util.List;

public interface LoanService {

    // Shows how many days the borrower wants the asset for, used with the asset's dailyRate to calculate loan total cost
    Loan requestLoan(Long assetId, String email, Integer durationDays);

    Loan approveLoan(Long loanId);

    Loan rejectLoan(Long loanId);

    Loan checkOut(Long loanId);

    Loan returnAsset(Long loanId);

    List<Loan> getAllLoans();

    List<Loan> getUserLoans(String email);

    List<Loan> getOverdueLoans();

    // Sweeps all checked-out loans and flags any whose due date has passed as OVERDUE
    void updateOverdueLoans();

    //This Sweeps checked-out loans due within the next 48 hours
    void sendDueSoonWarnings();
}