package com.example.evsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/docs", "/error", "/favicon.ico", "/ui/home", "/ui/login", "/ui/register", "/css/**", "/js/**", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/stations/**", "/chargers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/stations/**", "/chargers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/stations/**", "/chargers/**", "/reservations/**").hasRole("ADMIN")
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
                        .requestMatchers("/ui/**", "/vehicles/**", "/stations/**", "/reservations/**", "/sessions/**", "/chargers/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/ui/login")
                        .loginProcessingUrl("/ui/login")
                        .defaultSuccessUrl("/ui/home", true)
                        .failureUrl("/ui/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/ui/logout")
                        .logoutSuccessUrl("/ui/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
