package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    // Matching constructor: takes 2 arguments
    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void displayMenu() {
        while (true) {
            System.out.println("\n--- ADMIN MANAGEMENT PANEL ---");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout / Return to Main Menu");
            System.out.print("Select option (1-4): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingRegistrations();
                case "3" -> approveUserAccount();
                case "4" -> {
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
        System.out.print("Enter Target User ID to approve (Enter 0 to cancel): ");
        String input = scanner.nextLine().trim();

        try {
            int userId = Integer.parseInt(input);
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
            } else {
                System.out.println("[!] Failed to update user status in the database.");
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid input format. Please enter a valid numeric User ID.");
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