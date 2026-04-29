package com.example.evsystem.service;

import com.example.evsystem.dto.RegisterUserRequest;
import com.example.evsystem.entity.AppUser;
import com.example.evsystem.enums.UserRole;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppUserServiceTest {

    private AppUserRepository appUserRepository;
    private PasswordEncoder passwordEncoder;
    private AppUserService appUserService;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        appUserService = new AppUserService(appUserRepository, passwordEncoder);
    }

    @Test
    void registerCreatesEnabledRoleUserWithEncodedPassword() {
        RegisterUserRequest request = registerRequest("alice", "secret123", "secret123");

        when(appUserRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);
        when(appUserRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser user = appUserService.register(request);

        assertEquals("alice", user.getUsername());
        assertEquals(UserRole.ROLE_USER, user.getRole());
        assertTrue(user.isEnabled());
        assertNotEquals("secret123", user.getPasswordHash());
        assertTrue(passwordEncoder.matches("secret123", user.getPasswordHash()));
    }

    @Test
    void registerRejectsDuplicateUsername() {
        RegisterUserRequest request = registerRequest("alice", "secret123", "secret123");
        when(appUserRepository.existsByUsernameIgnoreCase("alice")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> appUserService.register(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void registerRejectsPasswordMismatch() {
        RegisterUserRequest request = registerRequest("alice", "secret123", "different123");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> appUserService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void createSeedUserCreatesRequestedAdminWhenMissing() {
        when(appUserRepository.existsByUsernameIgnoreCase("admin")).thenReturn(false);
        when(appUserRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        appUserService.createSeedUserIfMissing("admin", "admin-password", UserRole.ROLE_ADMIN);

        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void loadUserByUsernameReturnsGrantedRole() {
        AppUser user = new AppUser();
        user.setUsername("admin");
        user.setPasswordHash(passwordEncoder.encode("admin-password"));
        user.setRole(UserRole.ROLE_ADMIN);
        user.setEnabled(true);

        when(appUserRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));

        UserDetails details = appUserService.loadUserByUsername("admin");

        assertEquals("admin", details.getUsername());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
    }

    private RegisterUserRequest registerRequest(String username, String password, String confirmPassword) {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        return request;
    }
}
