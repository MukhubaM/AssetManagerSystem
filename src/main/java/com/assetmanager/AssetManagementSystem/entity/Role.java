package com.assetmanager.AssetManagementSystem.entity;

import java.util.List;

public enum Role {

    ADMIN,
    MANAGER,
    BORROWER;

    public static List<Role> selfRegistrable() {

        return List.of(BORROWER, MANAGER);
    }
}