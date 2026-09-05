package com.bugtracker.controller;

import com.bugtracker.entity.User;
import com.bugtracker.enums.Role;
import com.bugtracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller for user management operations.
 * Only accessible by users with ADMIN role.
 */
@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('MANAGER')")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Display list of all users.
     */
    @GetMapping
    public String listUsers(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.trim().isEmpty()) {
            model.addAttribute("users", userService.searchUsers(search.trim()));
            model.addAttribute("searchTerm", search);
        } else {
            model.addAttribute("users", userService.findAll());
        }
        
        // Add user statistics
        UserService.UserStats userStats = userService.getUserStats();
        model.addAttribute("userStats", userStats);
        
        return "users/list";
    }

    /**
     * Display form to create a new user.
     */
    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "users/form";
    }

    /**
     * Process user creation.
     */
    @PostMapping("/new")
    public String createUser(@Valid @ModelAttribute User user, 
                           BindingResult result, 
                           Model model, 
                           RedirectAttributes redirectAttributes) {
        
        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "users/form";
        }
        
        // Check if username already exists
        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username already exists");
            model.addAttribute("roles", Role.values());
            return "users/form";
        }
        
        // Check if email already exists
        if (userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists");
            model.addAttribute("roles", Role.values());
            return "users/form";
        }
        
        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
            return "redirect:/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating user: " + e.getMessage());
            model.addAttribute("roles", Role.values());
            return "users/form";
        }
    }

    /**
     * Display user details.
     */
    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/users";
        }
        
        model.addAttribute("user", userOpt.get());
        return "users/view";
    }

    /**
     * Display form to edit a user.
     */
    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/users";
        }
        
        model.addAttribute("user", userOpt.get());
        model.addAttribute("roles", Role.values());
        return "users/edit";
    }

    /**
     * Process user update.
     */
    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                           @Valid @ModelAttribute User user,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "users/edit";
        }
        
        // Check if username already exists for another user
        if (userService.existsByUsernameAndNotId(user.getUsername(), id)) {
            result.rejectValue("username", "error.user", "Username already exists");
            model.addAttribute("roles", Role.values());
            return "users/edit";
        }
        
        // Check if email already exists for another user
        if (userService.existsByEmailAndNotId(user.getEmail(), id)) {
            result.rejectValue("email", "error.user", "Email already exists");
            model.addAttribute("roles", Role.values());
            return "users/edit";
        }
        
        try {
            // Get existing user to preserve password and other fields
            Optional<User> existingUserOpt = userService.findById(id);
            if (existingUserOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found");
                return "redirect:/users";
            }
            
            User existingUser = existingUserOpt.get();
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setRole(user.getRole());
            existingUser.setEnabled(user.isEnabled());
            
            userService.updateUser(existingUser);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            return "redirect:/users/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating user: " + e.getMessage());
            model.addAttribute("roles", Role.values());
            return "users/edit";
        }
    }

    /**
     * Toggle user enabled status.
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/users";
        }
        
        User user = userOpt.get();
        userService.setUserEnabled(id, !user.isEnabled());
        
        String status = user.isEnabled() ? "disabled" : "enabled";
        redirectAttributes.addFlashAttribute("successMessage", "User " + status + " successfully!");
        
        return "redirect:/users";
    }

    /**
     * Display form to change user password.
     */
    @GetMapping("/{id}/change-password")
    public String changePasswordForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/users";
        }
        
        model.addAttribute("user", userOpt.get());
        return "users/change-password";
    }

    /**
     * Process password change.
     */
    @PostMapping("/{id}/change-password")
    public String changePassword(@PathVariable Long id,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        
        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters long");
            return "redirect:/users/" + id + "/change-password";
        }
        
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match");
            return "redirect:/users/" + id + "/change-password";
        }
        
        try {
            userService.updatePassword(id, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
            return "redirect:/users/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating password: " + e.getMessage());
            return "redirect:/users/" + id + "/change-password";
        }
    }

    /**
     * Delete a user.
     */
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found");
                return "redirect:/users";
            }
            
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        
        return "redirect:/users";
    }
}

