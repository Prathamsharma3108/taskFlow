package com.bugtracker.repository;

import com.bugtracker.entity.Bug;
import com.bugtracker.entity.User;
import com.bugtracker.enums.BugStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Bug entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods.
 */
@Repository
public interface BugRepository extends JpaRepository<Bug, Long> {

    /**
     * Find all bugs by status.
     * @param status the bug status to filter by
     * @return List of bugs with the specified status
     */
    List<Bug> findByStatus(BugStatus status);

    /**
     * Find all bugs reported by a specific user.
     * @param reporter the user who reported the bugs
     * @return List of bugs reported by the user
     */
    List<Bug> findByReporter(User reporter);

    /**
     * Find all bugs assigned to a specific user.
     * @param assignee the user assigned to the bugs
     * @return List of bugs assigned to the user
     */
    List<Bug> findByAssignee(User assignee);

    /**
     * Find all bugs by priority.
     * @param priority the priority level to filter by
     * @return List of bugs with the specified priority
     */
    List<Bug> findByPriority(String priority);

    /**
     * Find all unassigned bugs (assignee is null).
     * @return List of unassigned bugs
     */
    List<Bug> findByAssigneeIsNull();

    /**
     * Find all assigned bugs (assignee is not null).
     * @return List of assigned bugs
     */
    List<Bug> findByAssigneeIsNotNull();

    /**
     * Find bugs by status and assignee.
     * @param status the bug status
     * @param assignee the assigned user
     * @return List of bugs matching the criteria
     */
    List<Bug> findByStatusAndAssignee(BugStatus status, User assignee);

    /**
     * Find bugs by status and reporter.
     * @param status the bug status
     * @param reporter the reporting user
     * @return List of bugs matching the criteria
     */
    List<Bug> findByStatusAndReporter(BugStatus status, User reporter);

    /**
     * Find bugs created within a date range.
     * @param startDate the start date
     * @param endDate the end date
     * @return List of bugs created within the date range
     */
    List<Bug> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Search bugs by title or description containing the search term (case-insensitive).
     * @param searchTerm the term to search for
     * @return List of bugs matching the search criteria
     */
    @Query("SELECT b FROM Bug b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Bug> findByTitleOrDescriptionContaining(@Param("searchTerm") String searchTerm);

    /**
     * Count bugs by status.
     * @param status the bug status
     * @return number of bugs with the specified status
     */
    long countByStatus(BugStatus status);

    /**
     * Count bugs assigned to a specific user.
     * @param assignee the assigned user
     * @return number of bugs assigned to the user
     */
    long countByAssignee(User assignee);

    /**
     * Count bugs reported by a specific user.
     * @param reporter the reporting user
     * @return number of bugs reported by the user
     */
    long countByReporter(User reporter);

    /**
     * Find all bugs ordered by creation date (newest first).
     * @return List of bugs ordered by creation date descending
     */
    List<Bug> findAllByOrderByCreatedAtDesc();

    /**
     * Find bugs by multiple statuses.
     * @param statuses the list of statuses to filter by
     * @return List of bugs with any of the specified statuses
     */
    List<Bug> findByStatusIn(List<BugStatus> statuses);

    /**
     * Find recent bugs (created in the last N days).
     * @param daysAgo the number of days to look back
     * @return List of recent bugs
     */
    @Query("SELECT b FROM Bug b WHERE b.createdAt >= :daysAgo ORDER BY b.createdAt DESC")
    List<Bug> findRecentBugs(@Param("daysAgo") LocalDateTime daysAgo);

    /**
     * Find bugs assigned to a user with specific status.
     * @param assignee the assigned user
     * @param statuses the list of statuses to filter by
     * @return List of bugs matching the criteria
     */
    @Query("SELECT b FROM Bug b WHERE b.assignee = :assignee AND b.status IN :statuses ORDER BY b.createdAt DESC")
    List<Bug> findByAssigneeAndStatusIn(@Param("assignee") User assignee, @Param("statuses") List<BugStatus> statuses);
}

