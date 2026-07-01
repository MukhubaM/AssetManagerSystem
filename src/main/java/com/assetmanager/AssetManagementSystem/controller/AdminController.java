package com.assetmanager.AssetManagementSystem.controller;

import com.assetmanager.AssetManagementSystem.entity.Role;
import com.assetmanager.AssetManagementSystem.repository.AuditLogRepository;
import com.assetmanager.AssetManagementSystem.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/users")
    public String listUsers(Model model) {

        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", Role.values());

        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(
            @PathVariable
            Long id,
            @RequestParam
            Role role,
            Authentication auth) {

        userService.changeRole(id, role, auth.getName());

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/enable")
    public String enableUser(
            @PathVariable
            Long id,
            Authentication auth) {

        userService.setEnabled(id, true, auth.getName());

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/disable")
    public String disableUser(
            @PathVariable
            Long id,
            Authentication auth) {

        userService.setEnabled(id, false, auth.getName());

        return "redirect:/admin/users";
    }

    @GetMapping("/audit-logs")
    public String auditLogs(Model model) {

        model.addAttribute("logs", auditLogRepository.findAllByOrderByTimestampDesc());

        return "admin/audit-logs";
    }
}