package com.assetmanager.AssetManagementSystem.security;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**")
                        .permitAll()

                        //  This is what sets ADMIN apart from MANAGER: managers run day-to-day asset/loan operations, only admins manage the system and its users.
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // Asset management (create/edit/retire) : staff only
                        .requestMatchers("/assets/create", "/assets/*/retire", "/assets/*/edit")
                        .hasAnyRole("ADMIN", "MANAGER")

                        // Reports : for staff only
                        .requestMatchers("/assets/report", "/loans/history", "/loans/overdue")
                        .hasAnyRole("ADMIN", "MANAGER")

                        // Loan lifecycle management : staff only
                        .requestMatchers("/loans/approve/**", "/loans/reject/**", "/loans/checkout/**")
                        .hasAnyRole("ADMIN", "MANAGER")

                        // For borrowers to request and return their own loans
                        .requestMatchers("/loans/request/**", "/loans/return/**", "/loans/my")
                        .hasAnyRole("ADMIN", "MANAGER", "BORROWER")

                        // Full loan list : staff only
                        .requestMatchers("/loans")
                        .hasAnyRole("ADMIN", "MANAGER")
                        .anyRequest()
                        .authenticated()    // Everything else is authenticated
                )

                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );

        return http.build();
    }
}