package com.scc.ui;

import com.scc.dao.*;
import com.scc.model.*;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final PassengerDao passengerDao;
    private final MaintenanceDao maintenanceDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, PassengerDao passengerDao, MaintenanceDao maintenanceDao, Scanner scanner) {
        this.userDao = userDao;
        this.passengerDao = passengerDao;
        this.maintenanceDao = maintenanceDao;
        this.scanner = scanner;
    }

    public void displayMenu() {
        while (true) {
            System.out.println("\n--- ADMIN MANAGEMENT PANEL ---");
            System.out.println("1. User Accounts Management");
            System.out.println("2. Fleet & Operations Monitoring");
            System.out.println("3. Financial Reports & Summary");
            System.out.println("4. Review Fare Increase Requests");
            System.out.println("5. Logout / Return to Main Menu");
            System.out.print("Select option (1-5): ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                manageUsers();
            } else if (choice.equals("2")) {
                manageFleet();
            } else if (choice.equals("3")) {
                showFinancialReport();
            } else if (choice.equals("4")) {
                reviewFareRequests();
            } else if (choice.equals("5")) {
                System.out.println("[LOGOUT] Returning to main menu.");
                break;
            } else {
                System.out.println("Invalid selection!");
            }
        }
    }

    private void manageUsers() {
        while (true) {
            System.out.println("\n--- USER ACCOUNTS MANAGEMENT ---");
            System.out.println("1. View Pending Registrations");
            System.out.println("2. Approve / Reject Pending Account");
            System.out.println("3. Delete Pending Account");
            System.out.println("4. View All Drivers & Conductors");
            System.out.println("5. Delete Driver or Conductor Account");
            System.out.println("6. Back to Main Admin Menu");
            System.out.print("Select option (1-6): ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                List<User> pending = userDao.getPendingUsers();
                if (pending.isEmpty()) System.out.println("No pending user approvals.");
                else pending.forEach(System.out::println);
            } else if (choice.equals("2")) {
                System.out.print("Enter User ID to update: ");
                try {
                    int id = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Action [1] Approve | [2] Reject: ");
                    String act = scanner.nextLine().trim();
                    String status = act.equals("1") ? "APPROVED" : "REJECTED";
                    if (userDao.updateUserStatus(id, status)) {
                        System.out.println("User status updated to " + status);
                    }
                } catch (NumberFormatException e) { System.out.println("Invalid ID format!"); }
            } else if (choice.equals("3")) {
                System.out.print("Enter Pending User ID to delete: ");
                try {
                    int id = Integer.parseInt(scanner.nextLine().trim());
                    if (userDao.deleteUser(id)) System.out.println("[SUCCESS] Pending account deleted!");
                    else System.out.println("[!] User not found or not in PENDING state.");
                } catch (NumberFormatException e) { System.out.println("Invalid ID format!"); }
            } else if (choice.equals("4")) {
                List<User> staff = userDao.getStaffUsers();
                if (staff.isEmpty()) System.out.println("No registered drivers or conductors found.");
                else staff.forEach(System.out::println);
            } else if (choice.equals("5")) {
                List<User> staff = userDao.getStaffUsers();
                if (staff.isEmpty()) {
                    System.out.println("No staff accounts available to delete.");
                } else {
                    staff.forEach(System.out::println);
                    System.out.print("Enter Driver/Conductor User ID to delete: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine().trim());
                        if (userDao.deleteStaffUser(id)) System.out.println("[SUCCESS] Staff account deleted!");
                        else System.out.println("[!] Invalid ID or target is not a driver/conductor.");
                    } catch (NumberFormatException e) { System.out.println("Invalid ID format!"); }
                }
            } else if (choice.equals("6")) {
                break;
            }
        }
    }

    private void manageFleet() {
        while (true) {
            System.out.println("\n--- FLEET & OPERATIONS ---");
            System.out.println("1. View Full Passenger Safety Log");
            System.out.println("2. View Vehicle Maintenance Checklist");
            System.out.println("3. Back to Main Admin Menu");
            System.out.print("Select option (1-3): ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                List<Passenger> allPassengers = passengerDao.getAllPassengersHistory();
                if (allPassengers.isEmpty()) System.out.println("No passenger records found.");
                else allPassengers.forEach(p -> System.out.printf("[%d] Time: %s | Name: %s | Dest: %s (%.1f km) | Status: %s\n",
                        p.getId(), p.getCreatedAt(), p.getName(), p.getDestination(), p.getDistance(), p.getStatus()));
            } else if (choice.equals("2")) {
                List<MaintenanceItem> items = maintenanceDao.getChecklist();
                items.forEach(i -> System.out.printf("[%d] %s -> Status: %s\n", i.getId(), i.getItem(), i.getStatus()));
            } else if (choice.equals("3")) {
                break;
            }
        }
    }

    private void showFinancialReport() {
        double gross = passengerDao.getTotalEarnings();
        double exp = maintenanceDao.getTotalExpenses();
        double net = gross - exp;

        System.out.println("\n==================================");
        System.out.println("      ADMIN FINANCIAL REPORT      ");
        System.out.println("==================================");
        System.out.printf("Total Jeep Earnings (Gross) : P%.2f\n", gross);
        System.out.printf("Total Expenses Logged       : P%.2f\n", exp);
        System.out.printf("Net Operational Income      : P%.2f\n", net);
        System.out.println("==================================");
    }

    private void reviewFareRequests() {
        List<FareRequest> requests = passengerDao.getPendingFareRequests();
        if (requests.isEmpty()) {
            System.out.println("No pending fare increase requests.");
        } else {
            requests.forEach(System.out::println);
            System.out.print("Enter Request ID to process (0 to go back): ");
            try {
                int reqId = Integer.parseInt(scanner.nextLine().trim());
                if (reqId > 0) {
                    System.out.print("Action: [1] Approve & Apply Fare | [2] Reject: ");
                    boolean approve = scanner.nextLine().trim().equals("1");
                    passengerDao.processFareRequest(reqId, approve);
                    System.out.println(approve ? "[SUCCESS] Fare approved and applied!" : "[SUCCESS] Request rejected.");
                }
            } catch (NumberFormatException e) { System.out.println("Invalid input!"); }
        }
    }
}