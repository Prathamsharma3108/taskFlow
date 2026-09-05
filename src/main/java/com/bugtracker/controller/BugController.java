package com.bugtracker.controller;

import com.bugtracker.entity.Bug;
import com.bugtracker.entity.User;
import com.bugtracker.enums.BugStatus;
import com.bugtracker.service.BugService;
import com.bugtracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Controller for bug management operations.
 * Handles bug creation, viewing, assignment, and status updates.
 */
@Controller
@RequestMapping("/bugs")
public class BugController {

    private final BugService bugService;
    private final UserService userService;

    @Autowired
    public BugController(BugService bugService, UserService userService) {
        this.bugService = bugService;
        this.userService = userService;
    }

    /**
     * Display list of all bugs.
     */
    @GetMapping
    public String listBugs(@RequestParam(value = "status", required = false) String status,
                          @RequestParam(value = "search", required = false) String search,
                          Model model) {
        
        List<Bug> bugs;
        
        if (search != null && !search.trim().isEmpty()) {
            bugs = bugService.searchBugs(search.trim());
            model.addAttribute("searchTerm", search);
        } else if (status != null && !status.isEmpty()) {
            try {
                BugStatus bugStatus = BugStatus.valueOf(status.toUpperCase());
                bugs = bugService.findByStatus(bugStatus);
                model.addAttribute("selectedStatus", status);
            } catch (IllegalArgumentException e) {
                bugs = bugService.findAll();
            }
        } else {
            bugs = bugService.findAll();
        }
        
        model.addAttribute("bugs", bugs);
        model.addAttribute("bugStatuses", BugStatus.values());
        model.addAttribute("currentUser", getCurrentUser());
        
        // Add bug statistics
        BugService.BugStats bugStats = bugService.getBugStats();
        model.addAttribute("bugStats", bugStats);
        
        return "bugs/list";
    }

    /**
     * Display form to create a new bug.
     */
    @GetMapping("/new")
    public String newBugForm(Model model) {
        Bug bug = new Bug();
        bug.setReporter(getCurrentUser());
        
        model.addAttribute("bug", bug);
        model.addAttribute("priorities", Arrays.asList("Low", "Medium", "High", "Critical"));
        model.addAttribute("assignableUsers", userService.findAssignableUsers());
        
        return "bugs/form";
    }

    /**
     * Process bug creation.
     */
    @PostMapping("/new")
    public String createBug(@Valid @ModelAttribute Bug bug,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("priorities", Arrays.asList("Low", "Medium", "High", "Critical"));
            model.addAttribute("assignableUsers", userService.findAssignableUsers());
            return "bugs/form";
        }
        
        try {
            bug.setReporter(getCurrentUser());
            bug.setStatus(BugStatus.OPEN);
            
            Bug savedBug = bugService.createBug(bug);
            redirectAttributes.addFlashAttribute("successMessage", "Bug created successfully!");
            return "redirect:/bugs/" + savedBug.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating bug: " + e.getMessage());
            model.addAttribute("priorities", Arrays.asList("Low", "Medium", "High", "Critical"));
            model.addAttribute("assignableUsers", userService.findAssignableUsers());
            return "bugs/form";
        }
    }

    /**
     * Display bug details.
     */
    @GetMapping("/{id}")
    public String viewBug(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Bug> bugOpt = bugService.findById(id);
        if (bugOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bug not found");
            return "redirect:/bugs";
        }
        
        Bug bug = bugOpt.get();
        User currentUser = getCurrentUser();
        
        model.addAttribute("bug", bug);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("canEdit", bugService.canUserEditBug(bug, currentUser));
        model.addAttribute("canAssign", bugService.canUserAssignBugs(currentUser));
        model.addAttribute("bugStatuses", BugStatus.values());
        model.addAttribute("assignableUsers", userService.findAssignableUsers());
        
        return "bugs/view";
    }

    /**
     * Display form to edit a bug.
     */
    @GetMapping("/{id}/edit")
    public String editBugForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Bug> bugOpt = bugService.findById(id);
        if (bugOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bug not found");
            return "redirect:/bugs";
        }
        
        Bug bug = bugOpt.get();
        User currentUser = getCurrentUser();
        
        if (!bugService.canUserEditBug(bug, currentUser)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to edit this bug");
            return "redirect:/bugs/" + id;
        }
        
        model.addAttribute("bug", bug);
        model.addAttribute("priorities", Arrays.asList("Low", "Medium", "High", "Critical"));
        model.addAttribute("bugStatuses", BugStatus.values());
        model.addAttribute("assignableUsers", userService.findAssignableUsers());
        model.addAttribute("canAssign", bugService.canUserAssignBugs(currentUser));
        
        return "bugs/edit";
    }

    /**
     * Process bug update.
     */
    @PostMapping("/{id}/edit")
    public String updateBug(@PathVariable Long id,
                          @Valid @ModelAttribute Bug bug,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        
        Optional<Bug> existingBugOpt = bugService.findById(id);
        if (existingBugOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bug not found");
            return "redirect:/bugs";
        }
        
        Bug existingBug = existingBugOpt.get();
        User currentUser = getCurrentUser();
        
        if (!bugService.canUserEditBug(existingBug, currentUser)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to edit this bug");
            return "redirect:/bugs/" + id;
        }
        
        if (result.hasErrors()) {
            model.addAttribute("priorities", Arrays.asList("Low", "Medium", "High", "Critical"));
            model.addAttribute("bugStatuses", BugStatus.values());
            model.addAttribute("assignableUsers", userService.findAssignableUsers());
            model.addAttribute("canAssign", bugService.canUserAssignBugs(currentUser));
            return "bugs/edit";
        }
        
        try {
            // Update only allowed fields
            existingBug.setTitle(bug.getTitle());
            existingBug.setDescription(bug.getDescription());
            existingBug.setPriority(bug.getPriority());
            existingBug.setType(bug.getType());
            
            // Only allow status and assignee changes if user has permission
            if (bugService.canUserAssignBugs(currentUser)) {
                existingBug.setStatus(bug.getStatus());
                existingBug.setAssignee(bug.getAssignee());
            }
            
            bugService.updateBug(existingBug);
            redirectAttributes.addFlashAttribute("successMessage", "Bug updated successfully!");
            return "redirect:/bugs/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating bug: " + e.getMessage());
            model.addAttribute("priorities", Arrays.asList("Low", "Medium", "High", "Critical"));
            model.addAttribute("bugStatuses", BugStatus.values());
            model.addAttribute("assignableUsers", userService.findAssignableUsers());
            model.addAttribute("canAssign", bugService.canUserAssignBugs(currentUser));
            return "bugs/edit";
        }
    }

    /**
     * Assign bug to a user.
     */
    @PostMapping("/{id}/assign")
    public String assignBug(@PathVariable Long id,
                          @RequestParam(required = false) Long assigneeId,
                          RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser();
        if (!bugService.canUserAssignBugs(currentUser)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to assign bugs");
            return "redirect:/bugs/" + id;
        }
        
        try {
            User assignee = null;
            if (assigneeId != null) {
                Optional<User> assigneeOpt = userService.findById(assigneeId);
                if (assigneeOpt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Assignee not found");
                    return "redirect:/bugs/" + id;
                }
                assignee = assigneeOpt.get();
            }
            
            Bug updatedBug = bugService.assignBug(id, assignee);
            if (updatedBug == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bug not found");
                return "redirect:/bugs";
            }
            
            String message = assignee != null 
                ? "Bug assigned to " + assignee.getFullName() + " successfully!"
                : "Bug unassigned successfully!";
            redirectAttributes.addFlashAttribute("successMessage", message);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error assigning bug: " + e.getMessage());
        }
        
        return "redirect:/bugs/" + id;
    }

    /**
     * Update bug status.
     */
    @PostMapping("/{id}/update-status")
    public String updateBugStatus(@PathVariable Long id,
                                @RequestParam String status,
                                RedirectAttributes redirectAttributes) {
        
        // User currentUser = getCurrentUser();
        // if (!bugService.canUserAssignBugs(currentUser)) {
        //     redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to update bug status");
        //     return "redirect:/bugs/" + id;
        // }
        
        try {
            BugStatus bugStatus = BugStatus.valueOf(status.toUpperCase());
            Bug updatedBug = bugService.updateBugStatus(id, bugStatus);
            
            if (updatedBug == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bug not found");
                return "redirect:/bugs";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Task status updated to " + bugStatus.getDisplayName() + " successfully!");
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid status value");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating bug status: " + e.getMessage());
        }
        
        return "redirect:/bugs/" + id;
    }

    /**
     * Delete a bug.
     */
    @PostMapping("/{id}/delete")
    public String deleteBug(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Bug> bugOpt = bugService.findById(id);
        if (bugOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bug not found");
            return "redirect:/bugs";
        }
        
        Bug bug = bugOpt.get();
        User currentUser = getCurrentUser();
        
        // Only admin or the reporter can delete a bug
        if (!currentUser.getRole().name().equals("MANAGER") && 
            !bug.getReporter().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to delete this bug");
            return "redirect:/bugs/" + id;
        }
        
        try {
            bugService.deleteBug(id);
            redirectAttributes.addFlashAttribute("successMessage", "Bug deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting bug: " + e.getMessage());
        }
        
        return "redirect:/bugs";
    }

    /**
     * Display bugs assigned to current user.
     */
    @GetMapping("/my-assignments")
    public String myAssignments(Model model) {
        User currentUser = getCurrentUser();
        List<Bug> assignedBugs = bugService.findByAssignee(currentUser);
        
        model.addAttribute("bugs", assignedBugs);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pageTitle", "My Assigned Bugs");
        
        return "bugs/my-assignments";
    }

    /**
     * Display bugs reported by current user.
     */
    @GetMapping("/my-reports")
    public String myReports(Model model) {
        User currentUser = getCurrentUser();
        List<Bug> reportedBugs = bugService.findByReporter(currentUser);
        
        model.addAttribute("bugs", reportedBugs);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pageTitle", "My Reported Bugs");
        
        return "bugs/my-reports";
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

