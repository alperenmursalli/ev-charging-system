package com.example.evsystem.controller;

import com.example.evsystem.dto.AppUserResponse;
import com.example.evsystem.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@Tag(name = "Users", description = "Admin user management endpoints")
public class UserController {

    private final AppUserService appUserService;

    public UserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Operation(summary = "List users", description = "Returns all application users. Admin only.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppUserResponse> getAllUsers() {
        return appUserService.getAllUsers().stream()
                .map(AppUserResponse::from)
                .toList();
    }
}
