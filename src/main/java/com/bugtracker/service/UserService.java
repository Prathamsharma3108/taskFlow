package com.bugtracker.service;

import com.bugtracker.entity.User;
import com.bugtracker.enums.Role;
import com.bugtracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing User entities.
 * Provides business logic for user operations.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Find all users.
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Find user by ID.
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find user by username.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find all users by role.
     */
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    /**
     * Find all enabled users.
     */
    public List<User> findEnabledUsers() {
        return userRepository.findByEnabledTrue();
    }

    /**
     * Find users who can be assigned to bugs (developers and testers).
     */
    public List<User> findAssignableUsers() {
        return userRepository.findAssignableUsers();
    }

    /**
     * Search users by name or username.
     */
    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByNameContaining(searchTerm);
    }

    /**
     * Create a new user.
     */
    public User createUser(User user) {
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Update an existing user.
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Update user password.
     */
    public void updatePassword(Long userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
    }

    /**
     * Enable or disable a user.
     */
    public void setUserEnabled(Long userId, boolean enabled) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(enabled);
            userRepository.save(user);
        }
    }

    /**
     * Delete a user by ID.
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Check if username already exists.
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email already exists.
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if username exists for a different user (for updates).
     */
    public boolean existsByUsernameAndNotId(String username, Long id) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.isPresent() && !user.get().getId().equals(id);
    }

    /**
     * Check if email exists for a different user (for updates).
     */
    public boolean existsByEmailAndNotId(String email, Long id) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent() && !user.get().getId().equals(id);
    }

    /**
     * Get user statistics.
     */
    public UserStats getUserStats() {
        long totalUsers = userRepository.count();
        // long adminCount = userRepository.countByRole(Role.ADMIN);
        // long developerCount = userRepository.countByRole(Role.DEVELOPER);
        // long testerCount = userRepository.countByRole(Role.TESTER);
        
        // return new UserStats(totalUsers, adminCount, developerCount, testerCount);

        long managerCount = userRepository.countByRole(Role.MANAGER);
        long teamMemberCount = userRepository.countByRole(Role.TEAM_MEMBER);

        return new UserStats(totalUsers, managerCount, teamMemberCount);
    }

    /**
     * Inner class for user statistics.
     */
    public static class UserStats {
        private final long totalUsers;
        // private final long adminCount;
        // private final long developerCount;
        // private final long testerCount;

        private final long managerCount;
        private final long teamMemberCount;

        public UserStats(long totalUsers, long managerCount, long teamMemberCount) {
            this.totalUsers = totalUsers;
            this.managerCount = managerCount;
            this.teamMemberCount = teamMemberCount;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        // public long getAdminCount() { return adminCount; }
        // public long getDeveloperCount() { return developerCount; }
        // public long getTesterCount() { return testerCount; }

        public long getManagerCount() { return managerCount; }
        public long getTeamMemberCount() { return teamMemberCount; }
    }
}

