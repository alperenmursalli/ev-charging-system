package com.example.evsystem.config;

import com.example.evsystem.enums.UserRole;
import com.example.evsystem.service.AppUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserSeedConfig {

    @Bean
    ApplicationRunner userSeedRunner(
            AppUserService appUserService,
            @Value("${app.security.seed-admin-username:admin}") String adminUsername,
            @Value("${app.security.seed-admin-password:}") String adminPassword
    ) {
        return args -> {
            if (adminPassword == null || adminPassword.isBlank()) {
                throw new IllegalStateException("app.security.seed-admin-password must be configured.");
            }
            appUserService.createSeedUserIfMissing(adminUsername, adminPassword, UserRole.ROLE_ADMIN);
        };
    }
}
