package com.example.evsystem.service;

import com.example.evsystem.entity.AppUser;
import com.example.evsystem.enums.UserRole;
import com.example.evsystem.repository.AppUserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return appUserRepository.findByUsernameIgnoreCase(authentication.getName());
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserAdmin() {
        return getCurrentUser()
                .map(user -> user.getRole() == UserRole.ROLE_ADMIN)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Optional<String> getCurrentUsername() {
        return getCurrentUser().map(AppUser::getUsername);
    }
}
