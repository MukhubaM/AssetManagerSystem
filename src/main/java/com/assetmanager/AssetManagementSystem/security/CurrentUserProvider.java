package com.assetmanager.AssetManagementSystem.security;

import com.assetmanager.AssetManagementSystem.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    public Optional<User> getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return Optional.empty();
        }

        return Optional.of(userDetails.getUser());
    }

    public Long getCurrentUserId() {
        return getCurrentUser().map(User::getUserId).orElse(null);
    }
}
