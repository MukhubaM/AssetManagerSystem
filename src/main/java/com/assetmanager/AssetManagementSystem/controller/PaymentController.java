package com.assetmanager.AssetManagementSystem.controller;

import com.assetmanager.AssetManagementSystem.entity.Payment;
import com.assetmanager.AssetManagementSystem.service.PaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate/{loanId}")
    public String initiate(
            @PathVariable
            Long loanId,
            Authentication auth) {

        Payment payment = paymentService.initiatePayment(loanId, auth.getName());

        return "redirect:/payments/" + payment.getPaymentId();
    }

    @GetMapping("/{id}")
    public String view(
            @PathVariable
            Long id,
            Model model) {

        model.addAttribute("payment", paymentService.getPayment(id));

        return "payments/detail";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(
            @PathVariable
            Long id,
            Authentication auth) {

        paymentService.confirmPayment(id, auth.getName());

        return "redirect:/payments/" + id;
    }

    @PostMapping("/{id}/rollback")
    public String rollback(
            @PathVariable
            Long id,
            Authentication auth) {

        paymentService.rollbackPayment(id, auth.getName());

        return isStaff(auth) ? "redirect:/loans" : "redirect:/loans/my";
    }

    private boolean isStaff(Authentication auth) {

        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
    }
}