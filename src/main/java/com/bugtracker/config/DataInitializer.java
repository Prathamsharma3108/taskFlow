package com.bugtracker.config;

import com.bugtracker.entity.User;
import com.bugtracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data initializer component that runs after application startup.
 * Ensures that user passwords are properly encoded if they aren't already.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if users exist and encode their passwords if needed
        for (User user : userRepository.findAll()) {
            String password = user.getPassword();
            
            // Check if password is already encoded (BCrypt hashes start with $2a$, $2b$, or $2y$)
            if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                System.out.println("Encoded password for user: " + user.getUsername());
            }
        }
    }
}

