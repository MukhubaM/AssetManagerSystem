package com.assetmanager.AssetManagementSystem.config;

import com.assetmanager.AssetManagementSystem.entity.User;
import com.assetmanager.AssetManagementSystem.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// For MemberNumber to be assigned automatically at registration time but accounts created before this change existed has null memberNumber
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberNumberBackfillRunner implements ApplicationRunner {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        List<User> usersMissingMemberNumber = userRepository.findByMemberNumberIsNull();

        if (usersMissingMemberNumber.isEmpty()) {

            return;
        }

        for (User user : usersMissingMemberNumber) {

            user.setMemberNumber("MEM-" + String.format("%06d", user.getUserId()));
        }

        userRepository.saveAll(usersMissingMemberNumber);

        log.info("Backfilled member numbers for {} existing account(s)", usersMissingMemberNumber.size());
    }
}
