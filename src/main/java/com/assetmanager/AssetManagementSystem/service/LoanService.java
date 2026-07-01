package com.assetmanager.AssetManagementSystem.service;

import com.assetmanager.AssetManagementSystem.entity.Loan;

import java.util.List;

public interface LoanService {

    Loan requestLoan(Long assetId, String email);

    Loan approveLoan(Long loanId);

    Loan rejectLoan(Long loanId);

    Loan checkOut(Long loanId);

    Loan returnAsset(Long loanId);

    List<Loan> getAllLoans();

    List<Loan> getUserLoans(String email);

    List<Loan> getOverdueLoans();

    void updateOverdueLoans();
}
