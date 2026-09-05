package com.bugtracker.enums;

/**
 * Enumeration representing the different statuses a bug can have in the tracking system.
 */
public enum BugStatus {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    private final String displayName;

    BugStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

