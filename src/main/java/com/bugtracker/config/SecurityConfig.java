package com.bugtracker.config;

import com.bugtracker.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security configuration class.
 * Configures authentication, authorization, and security settings.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Password encoder bean for encoding and verifying passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider that uses our custom UserDetailsService.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Security filter chain configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(authz -> authz
                // Public resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // .requestMatchers("/h2-console/**").permitAll() // H2 console for development
                .requestMatchers("/login", "/error").permitAll()
                
                // // Admin-only endpoints
                // .requestMatchers("/admin/**", "/users/**").hasRole("ADMIN")
                
                // // Bug management - all authenticated users can view
                // .requestMatchers("/bugs/view/**", "/bugs/search").hasAnyRole("ADMIN", "DEVELOPER", "TESTER")
                
                // // Bug creation - all authenticated users
                // .requestMatchers("/bugs/create", "/bugs/new").hasAnyRole("ADMIN", "DEVELOPER", "TESTER")
                
                // // Bug assignment and status updates - developers and admins
                // .requestMatchers("/bugs/assign/**", "/bugs/update-status/**").hasAnyRole("ADMIN", "DEVELOPER")
                
                // // Bug editing - reporters, assignees, and admins
                // .requestMatchers("/bugs/edit/**").hasAnyRole("ADMIN", "DEVELOPER", "TESTER")
                
                // // Dashboard and general bug listing
                // .requestMatchers("/", "/dashboard", "/bugs", "/bugs/").hasAnyRole("ADMIN", "DEVELOPER", "TESTER")
                
                .requestMatchers("/admin/**", "/users/**").hasRole("MANAGER")

                .requestMatchers("/bugs/view/**", "/bugs/search")
                .hasAnyRole("MANAGER", "TEAM_MEMBER")

                .requestMatchers("/bugs/create", "/bugs/new")
                .hasAnyRole("MANAGER", "TEAM_MEMBER")

                .requestMatchers("/bugs/assign/**")
                .hasRole("MANAGER")

                .requestMatchers("/bugs/update-status/**")
                .hasAnyRole("MANAGER", "TEAM_MEMBER")

                .requestMatchers("/bugs/edit/**")
                .hasAnyRole("MANAGER", "TEAM_MEMBER")

                .requestMatchers("/", "/dashboard", "/bugs", "/bugs/")
                .hasAnyRole("MANAGER", "TEAM_MEMBER")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );

        // Disable CSRF for H2 console (development only)
        // http.csrf(csrf -> csrf
        //     .ignoringRequestMatchers("/h2-console/**")
        // );
        
        // Allow frames for H2 console (development only)
        // http.headers(headers -> headers
        //     .frameOptions().sameOrigin()
        // );

        return http.build();
    }
}

