package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.Scanner;

public class Authentication {
    private final UserDao userDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println("\n=========================================");
            System.out.println(" TJOMS: TRADITIONAL JEEPNEY MANAGEMENT ");
            System.out.println("=========================================");
            System.out.println("1. Login");
            System.out.println("2. Register Account");
            System.out.println("3. Exit Application");
            System.out.print("Choose option (1-3): ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                handleLogin();
            } else if (choice.equals("2")) {
                handleRegistration();
            } else if (choice.equals("3")) {
                System.out.println("Thank you for using TJOMS. Safe trip!");
                break;
            } else {
                System.out.println("[!] Invalid choice. Please select 1, 2, or 3.");
            }
        }
    }

    private void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User account = userDao.loginUser(username, password);
        if (account == null) {
            System.out.println("[!] Invalid username or password!");
        } else if (account.status().equalsIgnoreCase("PENDING")) {
            System.out.println("[!] Account is pending admin approval.");
        } else if (account.status().equalsIgnoreCase("REJECTED")) {
            System.out.println("[!] Account has been rejected.");
        } else {
            System.out.printf("\n[SUCCESS] Welcome %s (%s)!\n", account.username(), account.roles());
            routeUser(account);
        }
    }

    private void handleRegistration() {
        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Enter Role [ADMIN / DRIVER / CONDUCTOR]: ");
        String role = scanner.nextLine().trim().toUpperCase();

        boolean registered = userDao.registerUser(username, password, role);
        if (registered) {
            System.out.println("[SUCCESS] Account registered successfully! Status is PENDING admin approval.");
        } else {
            System.out.println("[!] Registration failed. Username may already exist.");
        }
    }

    private void routeUser(User account) {
        String role = account.roles().toUpperCase();
        if (role.equals("ADMIN") || role.equals("ADMINISTRATOR")) {
            AdminMenu adminMenu = new AdminMenu(userDao, scanner);
            adminMenu.displayMenu();
        } else if (role.equals("CONDUCTOR")) {
            System.out.println("[INFO] Conductor dashboard active.");
        } else if (role.equals("DRIVER")) {
            System.out.println("[INFO] Driver dashboard active.");
        } else {
            System.out.println("[!] Role not recognized: " + role);
        }
    }
}