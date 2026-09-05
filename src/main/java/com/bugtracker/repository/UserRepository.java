package com.bugtracker.repository;

import com.bugtracker.entity.User;
import com.bugtracker.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username.
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email.
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all users by role.
     * @param role the role to filter by
     * @return List of users with the specified role
     */
    List<User> findByRole(Role role);

    /**
     * Find all enabled users.
     * @return List of enabled users
     */
    List<User> findByEnabledTrue();

    /**
     * Find all users by role and enabled status.
     * @param role the role to filter by
     * @param enabled the enabled status to filter by
     * @return List of users matching the criteria
     */
    List<User> findByRoleAndEnabled(Role role, boolean enabled);

    /**
     * Check if a username already exists.
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email already exists.
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find users by first name or last name containing the search term (case-insensitive).
     * @param searchTerm the term to search for
     * @return List of users matching the search criteria
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByNameContaining(@Param("searchTerm") String searchTerm);

    /**
     * Count users by role.
     * @param role the role to count
     * @return number of users with the specified role
     */
    long countByRole(Role role);

    /**
     * Find all developers and testers (users who can be assigned to bugs).
     * @return List of users who can be assigned to bugs
     */
    // @Query("SELECT u FROM User u WHERE u.role IN (com.bugtracker.enums.Role.DEVELOPER, com.bugtracker.enums.Role.TESTER) AND u.enabled = true")
    @Query("SELECT u FROM User u WHERE u.role IN (com.bugtracker.enums.Role.MANAGER, com.bugtracker.enums.Role.TEAM_MEMBER) AND u.enabled = true")
    List<User> findAssignableUsers();

}