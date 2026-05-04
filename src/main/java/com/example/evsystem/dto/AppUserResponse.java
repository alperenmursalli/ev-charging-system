package com.example.evsystem.dto;

import com.example.evsystem.entity.AppUser;
import com.example.evsystem.enums.UserRole;

public class AppUserResponse {

    private Long id;
    private String username;
    private UserRole role;
    private boolean enabled;

    public static AppUserResponse from(AppUser user) {
        AppUserResponse response = new AppUserResponse();
        response.id = user.getId();
        response.username = user.getUsername();
        response.role = user.getRole();
        response.enabled = user.isEnabled();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
