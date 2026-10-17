package com.scc;

import com.scc.dao.*;
import com.scc.model.*;
import com.scc.ui.*;

import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();
    private static final PassengerDao passengerDao = new PassengerDao();
    private static final MaintenanceDao maintenanceDao = new MaintenanceDao();

    public static void main(String[] args) {
        DatabaseConnection.initializeDatabase();

        while (true) {
            System.out.println("\n=========================================");
            System.out.println(" TJOMS: TRADITIONAL JEEPNEY MANAGEMENT ");
            System.out.println("=========================================");
            System.out.println("1. Login");
            System.out.println("2. Register Account");
            System.out.println("3. Exit Application");
            System.out.print("Choose option: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                handleLogin();
            } else if (choice.equals("2")) {
                System.out.print("Username: ");
                String u = sc.nextLine().trim();
                System.out.print("Password: ");
                String p = sc.nextLine().trim();
                System.out.print("Role [ADMIN / DRIVER / CONDUCTOR]: ");
                String r = sc.nextLine().trim().toUpperCase();
                userDao.registerUser(u, p, r);
            } else if (choice.equals("3")) {
                System.out.println("Safe trip!");
                sc.close();
                return;
            }
        }
    }

    private static void handleLogin() {
        System.out.print("Username: ");
        String u = sc.nextLine().trim();
        System.out.print("Password: ");
        String p = sc.nextLine().trim();

        User account = userDao.loginUser(u, p);
        if (account == null) {
            System.out.println("[!] Invalid credentials!");
        } else if (account.status().equalsIgnoreCase("PENDING")) {
            System.out.println("[!] Account pending admin approval.");
        } else if (account.status().equalsIgnoreCase("REJECTED")) {
            System.out.println("[!] Account rejected.");
        } else {
            System.out.printf("\nWelcome %s (%s)\n", account.username(), account.roles());
            String role = account.roles().toUpperCase();

            if (role.equals("ADMIN") || role.equals("ADMINISTRATOR")) {
                AdminMenu adminMenu = new AdminMenu(userDao, passengerDao, maintenanceDao, sc);
                adminMenu.displayMenu();
            } else if (role.equals("CONDUCTOR")) {
                ConductorMenu conductorMenu = new ConductorMenu(passengerDao, sc);
                conductorMenu.displayMenu();
            } else if (role.equals("DRIVER")) {
                DriverMenu driverMenu = new DriverMenu(passengerDao, maintenanceDao, sc, account.username());
                driverMenu.displayMenu();
            }
        }
    }
}