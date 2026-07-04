package com.assetmanager.AssetManagementSystem.repository;

import com.assetmanager.AssetManagementSystem.entity.Role;
import com.assetmanager.AssetManagementSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByRole(Role role);

    List<User> findAllByOrderByNameAsc();

    List<User> findByRoleAndEnabledTrue(Role role);

    // Used by the "forgot username/email" recovery flow, a borrower/manager proves their identity with employee number + ID number rather than an email they may have forgotten
    Optional<User> findByEmployeeNumberAndIdNumber(String employeeNumber, String idNumber);
}