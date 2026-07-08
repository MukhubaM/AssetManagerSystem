package com.assetmanager.AssetManagementSystem.repository;

import com.assetmanager.AssetManagementSystem.entity.Loan;
import com.assetmanager.AssetManagementSystem.entity.LoanStatus;
import com.assetmanager.AssetManagementSystem.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByBorrower(User borrower);

    List<Loan> findByStatus(LoanStatus status);

    long countByBorrowerAndStatus(User borrower, LoanStatus status);

    long countByStatus(LoanStatus status);

    // Used by the 48-hour before-due warning sweep (only loans that are currently checked out and haven't already been warned)
    List<Loan> findByStatusAndDueSoonNotifiedFalse(LoanStatus status);
}