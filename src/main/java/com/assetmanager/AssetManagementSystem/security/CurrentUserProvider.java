package com.assetmanager.AssetManagementSystem.security;

import com.assetmanager.AssetManagementSystem.entity.User;
import com.assetmanager.AssetManagementSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public Optional<User> getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {

            return Optional.empty();
        }

        return userRepository.findByEmail(userDetails.getUsername());
    }

    public Long getCurrentUserId() {

        return getCurrentUser().map(User::getUserId).orElse(null);
    }
}