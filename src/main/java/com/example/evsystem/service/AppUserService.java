package com.example.evsystem.service;

import com.example.evsystem.dto.RegisterUserRequest;
import com.example.evsystem.entity.AppUser;
import com.example.evsystem.enums.UserRole;
import com.example.evsystem.exception.BusinessException;
import com.example.evsystem.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser register(RegisterUserRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        if (username.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Username is required.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Password is required.");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
        }
        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Username is already in use.");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.ROLE_USER);
        user.setEnabled(true);
        return appUserRepository.save(user);
    }

    public void createSeedUserIfMissing(String username, String rawPassword, UserRole role) {
        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEnabled(true);
        appUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .authorities(new SimpleGrantedAuthority(appUser.getRole().name()))
                .disabled(!appUser.isEnabled())
                .build();
    }
}
