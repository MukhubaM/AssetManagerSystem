package com.assetmanager.AssetManagementSystem.controller;

import com.assetmanager.AssetManagementSystem.dto.LoanRequestForm;
import com.assetmanager.AssetManagementSystem.service.LoanService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/request/{assetId}")
    public String requestLoan(
            @PathVariable
            Long assetId,
            @Valid @ModelAttribute
            LoanRequestForm loanRequestForm,
            BindingResult result,
            Authentication auth) {

        if (result.hasErrors()) {

            return "redirect:/assets/" + assetId;
        }

        loanService.requestLoan(assetId, auth.getName(), loanRequestForm.getDurationDays());

        return "redirect:/assets";
    }

    @PostMapping("/approve/{id}")
    public String approveLoan(
            @PathVariable
            Long id) {

        loanService.approveLoan(id);

        return "redirect:/loans";
    }

    @PostMapping("/reject/{id}")
    public String rejectLoan(
            @PathVariable
            Long id) {

        loanService.rejectLoan(id);

        return "redirect:/loans";
    }

    @PostMapping("/checkout/{id}")
    public String checkoutLoan(
            @PathVariable
            Long id) {

        loanService.checkOut(id);

        return "redirect:/loans";
    }

    @PostMapping("/return/{id}")
    public String returnLoan(
            @PathVariable
            Long id,
            Authentication auth) {

        loanService.returnAsset(id);

        return isStaff(auth) ? "redirect:/loans" : "redirect:/loans/my";
    }

    @GetMapping
    public String allLoans(Model model) {

        model.addAttribute("loans", loanService.getAllLoans());

        return "loans/list";
    }

    @GetMapping("/my")
    public String myLoans(Model model, Authentication auth) {

        model.addAttribute("loans", loanService.getUserLoans(auth.getName()));

        return "loans/my-loans";
    }

    @GetMapping("/history")
    public String history(Model model) {

        model.addAttribute("loans", loanService.getAllLoans());

        return "reports/loan-history";
    }

    @GetMapping("/overdue")
    public String overdue(Model model) {

        model.addAttribute("loans", loanService.getOverdueLoans());

        return "reports/overdue";
    }

    private boolean isStaff(Authentication auth) {

        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
    }
}