package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner scanner;
    private final User currentAdmin;

    public AdminMenu(UserDao userDao, ActivityLogDao activityLogDao, Scanner scanner, User currentAdmin) {
        this.userDao = userDao;
        this.activityLogDao = activityLogDao;
        this.scanner = scanner;
        this.currentAdmin = currentAdmin;
    }

    public void displayMenu() {
        while (true) {
            System.out.println("\n--- ADMIN MANAGEMENT PANEL ---");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role & Status");
            System.out.println("5. Delete User Account");
            System.out.println("6. View System Audit Logs");
            System.out.println("7. Logout / Return to Main Menu");
            System.out.print("Select option (1-7): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingRegistrations();
                case "3" -> approveUserAccount();
                case "4" -> updateUserRoleAndStatus();
                case "5" -> deleteUserAccount();
                case "6" -> viewAuditLogsMenu();
                case "7" -> {
                    System.out.println("[LOGOUT] Returning to main menu.");
                    return;
                }
                default -> System.out.println("[!] Invalid selection!");
            }
        }
    }

    private void viewAllUsers() {
        List<User> users = userDao.getAllUsers();
        printUserTable(users, "ALL REGISTERED SYSTEM USERS");
    }

    private void viewPendingRegistrations() {
        List<User> pending = userDao.getPendingUsers();
        printUserTable(pending, "PENDING USER REGISTRATIONS");
    }

    private void approveUserAccount() {
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("\n[INFO] No pending user accounts to approve.");
            return;
        }

        printUserTable(pending, "PENDING ACCOUNTS FOR APPROVAL");
        int userId = readIntInput("Enter Target User ID to approve (Enter 0 to cancel): ");
        if (userId == 0) {
            System.out.println("[INFO] Approval action cancelled.");
            return;
        }

        boolean exists = pending.stream().anyMatch(u -> u.id() == userId);
        if (!exists) {
            System.out.println("[!] Error: User ID not found in the pending list.");
            return;
        }

        boolean success = userDao.approveUser(userId);
        if (success) {
            System.out.printf("[SUCCESS] User ID %d status updated from PENDING to APPROVED!\n", userId);
            // Auto Log Action
            activityLogDao.logAction(
                    currentAdmin.id(),
                    "USER_APPROVED",
                    String.format("Admin %s (ID %d) approved account for User ID %d", currentAdmin.username(), currentAdmin.id(), userId)
            );
        } else {
            System.out.println("[!] Failed to update user status in the database.");
        }
    }

    private void updateUserRoleAndStatus() {
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("\n[INFO] No users found in the system.");
            return;
        }

        printUserTable(users, "ALL USERS - UPDATE MANAGEMENT");
        int targetUserId = readIntInput("Enter Target User ID to update (Enter 0 to cancel): ");
        if (targetUserId == 0) {
            System.out.println("[INFO] Update action cancelled.");
            return;
        }

        if (targetUserId == currentAdmin.id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        boolean exists = users.stream().anyMatch(u -> u.id() == targetUserId);
        if (!exists) {
            System.out.println("[!] Error: User ID not found.");
            return;
        }

        String newRole = readValidRole();
        if (newRole.equals("CANCEL")) {
            System.out.println("[INFO] Update action cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus.equals("CANCEL")) {
            System.out.println("[INFO] Update action cancelled.");
            return;
        }

        boolean success = userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus);
        if (success) {
            System.out.printf("[SUCCESS] User ID %d updated successfully! Role: %s, Status: %s\n", targetUserId, newRole, newStatus);
            // Auto Log Action
            activityLogDao.logAction(
                    currentAdmin.id(),
                    "USER_UPDATED",
                    String.format("Admin %s (ID %d) changed role of User %d to %s and status to %s", currentAdmin.username(), currentAdmin.id(), targetUserId, newRole, newStatus)
            );
        } else {
            System.out.println("[!] Failed to update user in the database.");
        }
    }

    private void deleteUserAccount() {
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("\n[INFO] No users found in the system.");
            return;
        }

        printUserTable(users, "ALL USERS - DELETE MANAGEMENT");
        int targetUserId = readIntInput("Enter Target User ID to delete (Enter 0 to cancel): ");
        if (targetUserId == 0) {
            System.out.println("[INFO] Delete action cancelled.");
            return;
        }

        if (targetUserId == currentAdmin.id()) {
            System.out.println("[!] WARNING: Deleting the currently active admin account is restricted.");
            return;
        }

        boolean exists = users.stream().anyMatch(u -> u.id() == targetUserId);
        if (!exists) {
            System.out.println("[!] Error: User ID not found.");
            return;
        }

        System.out.print("Are you sure you want to permanently delete User ID " + targetUserId + "? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        if (!confirm.equals("Y")) {
            System.out.println("[INFO] Delete action cancelled.");
            return;
        }

        boolean success = userDao.deleteUser(targetUserId);
        if (success) {
            System.out.printf("[SUCCESS] User ID %d has been permanently deleted from the database.\n", targetUserId);
            // Auto Log Action
            activityLogDao.logAction(
                    currentAdmin.id(),
                    "USER_DELETED",
                    String.format("Admin %s (ID %d) deleted User ID %d", currentAdmin.username(), currentAdmin.id(), targetUserId)
            );
        } else {
            System.out.println("[!] Failed to delete user from the database.");
        }
    }

    private void viewAuditLogsMenu() {
        while (true) {
            System.out.println("\n--- SYSTEM AUDIT LOG INTERFACE ---");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("4. Return to Admin Menu");
            System.out.print("Select option (1-4): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    List<ActivityLog> logs = activityLogDao.getAllLogs();
                    printAuditLogTable(logs, "ALL SYSTEM AUDIT LOGS");
                }
                case "2" -> {
                    int targetUserId = readIntInput("Enter User ID to filter logs (Enter 0 to cancel): ");
                    if (targetUserId != 0) {
                        List<ActivityLog> userLogs = activityLogDao.getLogsByUserId(targetUserId);
                        printAuditLogTable(userLogs, "AUDIT LOGS FOR USER ID: " + targetUserId);
                    }
                }
                case "3" -> {
                    System.out.print("Are you sure you want to clear ALL historical logs? (Y/N): ");
                    String confirm = scanner.nextLine().trim().toUpperCase();
                    if (confirm.equals("Y")) {
                        boolean cleared = activityLogDao.clearAllLogs();
                        if (cleared) {
                            System.out.println("[SUCCESS] Historical audit logs cleared successfully.");
                            activityLogDao.logAction(currentAdmin.id(), "LOGS_CLEARED", "Admin cleared historical logs.");
                        } else {
                            System.out.println("[!] Failed to clear audit logs.");
                        }
                    } else {
                        System.out.println("[INFO] Clear logs action cancelled.");
                    }
                }
                case "4" -> {
                    return;
                }
                default -> System.out.println("[!] Invalid choice!");
            }
        }
    }

    private void printAuditLogTable(List<ActivityLog> logs, String title) {
        System.out.println("\n=========================================================================================================");
        System.out.println(" " + title);
        System.out.println("=========================================================================================================");
        if (logs.isEmpty()) {
            System.out.println(" (No audit log records found)");
            System.out.println("=========================================================================================================");
            return;
        }

        String format = "| %-5s | %-8s | %-18s | %-40s | %-20s |\n";
        String border = "+-------+----------+--------------------+------------------------------------------+----------------------+";

        System.out.println(border);
        System.out.printf(format, "ID", "USER_ID", "ACTION_TYPE", "DESCRIPTION", "TIMESTAMP");
        System.out.println(border);

        for (ActivityLog log : logs) {
            System.out.printf(format, log.id(), log.userId(), log.actionType(), log.description(), log.timestamp());
        }
        System.out.println(border);
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input. Please enter a valid numeric integer.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new Role (ADMIN, DRIVER, CONDUCTOR) [Type 0 to cancel]: ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (role.equals("0")) {
                return "CANCEL";
            }
            if (role.equals("ADMIN") || role.equals("DRIVER") || role.equals("CONDUCTOR")) {
                return role;
            }
            System.out.println("[!] Invalid role. Please enter ADMIN, DRIVER, or CONDUCTOR.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new Status (APPROVED, PENDING) [Type 0 to cancel]: ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("0")) {
                return "CANCEL";
            }
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("[!] Invalid status. Please enter APPROVED or PENDING.");
        }
    }

    private void printUserTable(List<User> users, String title) {
        System.out.println("\n=================================================================");
        System.out.println(" " + title);
        System.out.println("=================================================================");
        if (users.isEmpty()) {
            System.out.println(" (No records found)");
            System.out.println("=================================================================");
            return;
        }

        String format = "| %-5s | %-15s | %-12s | %-10s |\n";
        String border = "+-------+-----------------+--------------+------------+";

        System.out.println(border);
        System.out.printf(format, "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);

        for (User u : users) {
            System.out.printf(format, u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println(border);
    }
}