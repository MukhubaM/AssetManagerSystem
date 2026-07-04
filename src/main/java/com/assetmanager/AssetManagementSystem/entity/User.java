package com.assetmanager.AssetManagementSystem.entity;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    // Personal information
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate dateOfBirth;


    // Contact information
    @Column(unique = true)
    private String email;

    private String phone;

    private String address;


    // Employment information
    private String employeeNumber;

    @Column(unique = true)
    private String idNumber;

    private String department;

    private String position;


    // Account information
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String passwordHash;


    // This lets an ADMIN deactivate an account without deleting it (and not losing its loan history/audit trail)
    @Column(nullable = false)
    @ColumnDefault("true")
    @Builder.Default
    private boolean enabled = true;


    // Borrower-specific
    @Enumerated(EnumType.STRING)
    private BorrowerStatus borrowerStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private User supervisor;


    // Manager-specific
    // These permission flags are captured for record-keeping only
    @Enumerated(EnumType.STRING)
    private ManagerLevel managerLevel;

    private Boolean canApproveLoans;

    private Boolean canRegisterAssets;

    private Boolean canManageUsers;

    private Boolean canGenerateReports;


    // Profile/settings
    private String profilePicturePath;

    // A basic uploaded document(stored as ID document)
    private String documentPath;


    @Column(nullable = false)
    @ColumnDefault("true")
    @Builder.Default
    private boolean emailNotificationsEnabled = true;
}