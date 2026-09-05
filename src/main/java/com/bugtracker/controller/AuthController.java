package com.bugtracker.controller;

import com.bugtracker.entity.User;
import com.bugtracker.service.BugService;
import com.bugtracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for authentication-related endpoints.
 * Handles login, logout, and dashboard functionality.
 */
@Controller
public class AuthController {

    private final UserService userService;
    private final BugService bugService;

    @Autowired
    public AuthController(UserService userService, BugService bugService) {
        this.userService = userService;
        this.bugService = bugService;
    }

    /**
     * Display login page.
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password.");
        }
        
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        
        return "auth/login";
    }

    /**
     * Redirect root to dashboard.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    /**
     * Display dashboard with user-specific information.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        // Add user information to model
        model.addAttribute("currentUser", currentUser);
        
        // Add bug statistics
        BugService.BugStats bugStats = bugService.getBugStats();
        model.addAttribute("bugStats", bugStats);
        
        // Add user-specific bug statistics
        BugService.UserBugStats userBugStats = bugService.getUserBugStats(currentUser);
        model.addAttribute("userBugStats", userBugStats);
        
        // Add recent bugs
        model.addAttribute("recentBugs", bugService.findRecentBugs());
        
        // Add user's assigned bugs
        model.addAttribute("assignedBugs", bugService.findActiveBugsForUser(currentUser));
        
        // Add user statistics (for admins)
        if (currentUser.getRole().name().equals("MANAGER")) {
            UserService.UserStats userStats = userService.getUserStats();
            model.addAttribute("userStats", userStats);
        }
        
        return "dashboard";
    }

    /**
     * Display access denied page.
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }

    /**
     * Get current authenticated user.
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}

