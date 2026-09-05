// package com.bugtracker.enums;

// /**
//  * Enumeration representing the different roles a user can have in the bug tracker system.
//  */
// public enum Role {
//     ADMIN("Admin"),
//     DEVELOPER("Developer"), 
//     TESTER("Tester");

//     private final String displayName;

//     Role(String displayName) {
//         this.displayName = displayName;
//     }

//     public String getDisplayName() {
//         return displayName;
//     }

//     /**
//      * Returns the role name with ROLE_ prefix for Spring Security.
//      */
//     public String getAuthority() {
//         return "ROLE_" + this.name();
//     }
// }



package com.bugtracker.enums;

public enum Role {

    MANAGER("Manager"),
    TEAM_MEMBER("Team Member");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}