package com.bugtracker.service;

import com.bugtracker.entity.Bug;
import com.bugtracker.entity.User;
import com.bugtracker.enums.BugStatus;
import com.bugtracker.repository.BugRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Bug entities.
 * Provides business logic for bug operations.
 */
@Service
@Transactional
public class BugService {

    private final BugRepository bugRepository;

    @Autowired
    public BugService(BugRepository bugRepository) {
        this.bugRepository = bugRepository;
    }

    /**
     * Find all bugs.
     */
    public List<Bug> findAll() {
        return bugRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Find bug by ID.
     */
    public Optional<Bug> findById(Long id) {
        return bugRepository.findById(id);
    }

    /**
     * Find bugs by status.
     */
    public List<Bug> findByStatus(BugStatus status) {
        return bugRepository.findByStatus(status);
    }

    /**
     * Find bugs reported by a user.
     */
    public List<Bug> findByReporter(User reporter) {
        return bugRepository.findByReporter(reporter);
    }

    /**
     * Find bugs assigned to a user.
     */
    public List<Bug> findByAssignee(User assignee) {
        return bugRepository.findByAssignee(assignee);
    }

    /**
     * Find unassigned bugs.
     */
    public List<Bug> findUnassignedBugs() {
        return bugRepository.findByAssigneeIsNull();
    }

    /**
     * Find bugs by priority.
     */
    public List<Bug> findByPriority(String priority) {
        return bugRepository.findByPriority(priority);
    }

    /**
     * Search bugs by title or description.
     */
    public List<Bug> searchBugs(String searchTerm) {
        return bugRepository.findByTitleOrDescriptionContaining(searchTerm);
    }

    /**
     * Find recent bugs (last 7 days).
     */
    public List<Bug> findRecentBugs() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return bugRepository.findRecentBugs(sevenDaysAgo);
    }

    /**
     * Find active bugs for a user (assigned bugs that are not closed).
     */
    public List<Bug> findActiveBugsForUser(User user) {
        List<BugStatus> activeStatuses = Arrays.asList(BugStatus.OPEN, BugStatus.IN_PROGRESS, BugStatus.RESOLVED);
        return bugRepository.findByAssigneeAndStatusIn(user, activeStatuses);
    }

    /**
     * Create a new bug.
     */
    public Bug createBug(Bug bug) {
        return bugRepository.save(bug);
    }

    /**
     * Update an existing bug.
     */
    public Bug updateBug(Bug bug) {
        return bugRepository.save(bug);
    }

    /**
     * Assign a bug to a user.
     */
    public Bug assignBug(Long bugId, User assignee) {
        Optional<Bug> bugOpt = bugRepository.findById(bugId);
        if (bugOpt.isPresent()) {
            Bug bug = bugOpt.get();
            bug.setAssignee(assignee);
            
            // If bug is being assigned and status is OPEN, change to IN_PROGRESS
            if (assignee != null && bug.getStatus() == BugStatus.OPEN) {
                bug.setStatus(BugStatus.IN_PROGRESS);
            }
            
            return bugRepository.save(bug);
        }
        return null;
    }

    /**
     * Update bug status.
     */
    public Bug updateBugStatus(Long bugId, BugStatus status) {
        Optional<Bug> bugOpt = bugRepository.findById(bugId);
        if (bugOpt.isPresent()) {
            Bug bug = bugOpt.get();
            bug.setStatus(status);
            return bugRepository.save(bug);
        }
        return null;
    }

    /**
     * Update bug priority.
     */
    public Bug updateBugPriority(Long bugId, String priority) {
        Optional<Bug> bugOpt = bugRepository.findById(bugId);
        if (bugOpt.isPresent()) {
            Bug bug = bugOpt.get();
            bug.setPriority(priority);
            return bugRepository.save(bug);
        }
        return null;
    }

    /**
     * Delete a bug by ID.
     */
    public void deleteBug(Long id) {
        bugRepository.deleteById(id);
    }

    /**
     * Check if user can edit a bug (reporter, assignee, or admin role).
     */
    public boolean canUserEditBug(Bug bug, User user) {
        if (user == null || bug == null) {
            return false;
        }
        
        // Admin can edit any bug
        if (user.getRole().name().equals("MANAGER")) {
            return true;
        }
        
        // Reporter can edit their own bugs
        if (bug.getReporter() != null && bug.getReporter().getId().equals(user.getId())) {
            return true;
        }
        
        // Assignee can edit assigned bugs
        if (bug.getAssignee() != null && bug.getAssignee().getId().equals(user.getId())) {
            return true;
        }
        
        return false;
    }

    /**
     * Check if user can assign bugs (admin or developer role).
     */
    public boolean canUserAssignBugs(User user) {
        if (user == null) {
            return false;
        }
        return user.getRole().name().equals("MANAGER");
    }

    /**
     * Get bug statistics.
     */
    public BugStats getBugStats() {
        long totalBugs = bugRepository.count();
        long openBugs = bugRepository.countByStatus(BugStatus.OPEN);
        long inProgressBugs = bugRepository.countByStatus(BugStatus.IN_PROGRESS);
        long resolvedBugs = bugRepository.countByStatus(BugStatus.RESOLVED);
        long closedBugs = bugRepository.countByStatus(BugStatus.CLOSED);
        long unassignedBugs = bugRepository.findByAssigneeIsNull().size();
        
        return new BugStats(totalBugs, openBugs, inProgressBugs, resolvedBugs, closedBugs, unassignedBugs);
    }

    /**
     * Get bug statistics for a specific user.
     */
    public UserBugStats getUserBugStats(User user) {
        long reportedBugs = bugRepository.countByReporter(user);
        long assignedBugs = bugRepository.countByAssignee(user);
        List<Bug> activeBugs = findActiveBugsForUser(user);
        
        return new UserBugStats(reportedBugs, assignedBugs, activeBugs.size());
    }

    /**
     * Inner class for bug statistics.
     */
    public static class BugStats {
        private final long totalBugs;
        private final long openBugs;
        private final long inProgressBugs;
        private final long resolvedBugs;
        private final long closedBugs;
        private final long unassignedBugs;

        public BugStats(long totalBugs, long openBugs, long inProgressBugs, 
                       long resolvedBugs, long closedBugs, long unassignedBugs) {
            this.totalBugs = totalBugs;
            this.openBugs = openBugs;
            this.inProgressBugs = inProgressBugs;
            this.resolvedBugs = resolvedBugs;
            this.closedBugs = closedBugs;
            this.unassignedBugs = unassignedBugs;
        }

        // Getters
        public long getTotalBugs() { return totalBugs; }
        public long getOpenBugs() { return openBugs; }
        public long getInProgressBugs() { return inProgressBugs; }
        public long getResolvedBugs() { return resolvedBugs; }
        public long getClosedBugs() { return closedBugs; }
        public long getUnassignedBugs() { return unassignedBugs; }
    }

    /**
     * Inner class for user-specific bug statistics.
     */
    public static class UserBugStats {
        private final long reportedBugs;
        private final long assignedBugs;
        private final long activeBugs;

        public UserBugStats(long reportedBugs, long assignedBugs, long activeBugs) {
            this.reportedBugs = reportedBugs;
            this.assignedBugs = assignedBugs;
            this.activeBugs = activeBugs;
        }

        // Getters
        public long getReportedBugs() { return reportedBugs; }
        public long getAssignedBugs() { return assignedBugs; }
        public long getActiveBugs() { return activeBugs; }
    }
}

