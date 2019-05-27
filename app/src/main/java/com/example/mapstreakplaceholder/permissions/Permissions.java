package com.example.mapstreakplaceholder.permissions;

public enum Permissions {
    REQUEST_CHECK_SETTINGS(1000),
    REQUEST_LOCATION_PERMISSIONS(1001);

    private final int permissionCode;

    Permissions(int code) {
        permissionCode = code;
    }

    public int getPermissionCode() {
        return permissionCode;
    }
}
