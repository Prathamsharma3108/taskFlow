# TaskFlow – Smart Task Tracking System

TaskFlow is a full-stack task and bug tracking application built using Spring Boot, Spring Security, Thymeleaf, and PostgreSQL.  
The system enables managers and team members to collaborate efficiently by creating, assigning, tracking, and updating tasks through a secure role-based workflow.

---

# Features

## Authentication & Authorization
- Secure login system using Spring Security
- BCrypt password encryption
- Role-based access control
- Session management and authentication handling

---

## Task Management
- Create tasks with title, description, type, and priority
- Assign tasks to managers or team members
- Edit and update task details
- Track task progress through different statuses
- Delete tasks
- Search and filter tasks

---

## Dashboard
- Personalized dashboard for logged-in users
- Task statistics overview
- View assigned tasks
- View created tasks
- Quick navigation and workflow management

---

# User Roles

## Manager
- Create tasks
- Assign tasks
- Update task statuses
- Edit and delete tasks
- Manage workflow across the system

## Team Member
- View assigned tasks
- Update task statuses
- Edit accessible tasks
- Track task progress

---

# Tech Stack

## Backend
- Java 17
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate

## Frontend
- Thymeleaf
- HTML5
- CSS3

## Database
- PostgreSQL

## Build Tool
- Maven

---