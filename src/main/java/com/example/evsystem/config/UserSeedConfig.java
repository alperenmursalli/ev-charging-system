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
            @Value("${app.security.seed-admin-password:admin123}") String adminPassword,
            @Value("${app.security.seed-user-username:user}") String userUsername,
            @Value("${app.security.seed-user-password:user123}") String userPassword
    ) {
        return args -> {
            appUserService.createSeedUserIfMissing(adminUsername, adminPassword, UserRole.ROLE_ADMIN);
            appUserService.createSeedUserIfMissing(userUsername, userPassword, UserRole.ROLE_USER);
        };
    }
}
