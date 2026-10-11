package com.scc;

import com.scc.dao.*;
import com.scc.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();
    private static final PassengerDao passengerDao = new PassengerDao();
    private static final MaintenanceDao maintenanceDao = new MaintenanceDao();

    private static double baseKm = 4.0;
    private static double discountRate = 0.20;
    private static final int MAX_CAPACITY = 18;

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

            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> handleRegister();
                case "3" -> {
                    System.out.println("Shutting down system. Safe trip!");
                    sc.close();
                    return;
                }
                default -> System.out.println("Invalid selection!");
            }
        }
    }

    private static void handleRegister() {
        System.out.print("Enter new username: ");
        String user = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String pass = sc.nextLine().trim();
        System.out.print("Enter Role [ADMIN / DRIVER / CONDUCTOR]: ");
        String role = sc.nextLine().trim().toUpperCase();

        userDao.registerUser(user, pass, role);
    }

    private static void handleLogin() {
        System.out.print("Username: ");
        String user = sc.nextLine().trim();
        System.out.print("Password: ");
        String pass = sc.nextLine().trim();

        User account = userDao.loginUser(user, pass);
        if (account == null) {
            System.out.println("[!] Invalid username or password!");
        } else if (account.status().equalsIgnoreCase("PENDING")) {
            System.out.println("[!] Account approval is pending. Contact Administrator.");
        } else if (account.status().equalsIgnoreCase("REJECTED")) {
            System.out.println("[!] Account has been rejected by Administrator.");
        } else {
            System.out.printf("\nLogin Successful! Welcome %s (%s)\n", account.username(), account.roles());

            String role = account.roles().toUpperCase();
            if (role.equals("ADMIN") || role.equals("ADMINISTRATOR")) {
                adminPanel();
            } else if (role.equals("CONDUCTOR")) {
                conductorPanel();
            } else if (role.equals("DRIVER")) {
                driverPanel(account.username());
            } else {
                System.out.println("Unknown Role Profile.");
            }
        }
    }

    private static void adminPanel() {
        while (true) {
            System.out.println("\n--- ADMIN MANAGEMENT PANEL ---");
            System.out.println("1. User Accounts Management");
            System.out.println("2. Fleet & Operations Monitoring");
            System.out.println("3. Financial Reports & Summary");
            System.out.println("4. Review Fare Increase Requests");
            System.out.println("5. Logout / Return to Main Menu");
            System.out.print("Select option (1-5): ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                boolean managingUsers = true;
                while (managingUsers) {
                    System.out.println("\n--- USER ACCOUNTS MANAGEMENT ---");
                    System.out.println("1. View Pending Registrations");
                    System.out.println("2. Approve / Reject Pending Account");
                    System.out.println("3. Delete Pending Account");
                    System.out.println("4. View All Drivers & Conductors");
                    System.out.println("5. Delete Driver or Conductor Account");
                    System.out.println("6. Back to Main Admin Menu");
                    System.out.print("Select option (1-6): ");
                    String uChoice = sc.nextLine().trim();

                    if (uChoice.equals("1")) {
                        List<User> pending = userDao.getPendingUsers();
                        if (pending.isEmpty()) System.out.println("No pending user approvals.");
                        else pending.forEach(System.out::println);
                    } else if (uChoice.equals("2")) {
                        System.out.print("Enter User ID to update: ");
                        try {
                            int id = Integer.parseInt(sc.nextLine().trim());
                            System.out.print("Action [1] Approve | [2] Reject: ");
                            String act = sc.nextLine().trim();
                            String status = act.equals("1") ? "APPROVED" : "REJECTED";
                            if (userDao.updateUserStatus(id, status)) {
                                System.out.println("User status updated to " + status);
                            }
                        } catch (NumberFormatException e) { System.out.println("Invalid ID format!"); }
                    } else if (uChoice.equals("3")) {
                        System.out.print("Enter Pending User ID to delete: ");
                        try {
                            int id = Integer.parseInt(sc.nextLine().trim());
                            if (userDao.deleteUser(id)) System.out.println("[SUCCESS] Pending account deleted!");
                            else System.out.println("[!] User not found or not in PENDING state.");
                        } catch (NumberFormatException e) { System.out.println("Invalid ID format!"); }
                    } else if (uChoice.equals("4")) {
                        List<User> staff = userDao.getStaffUsers();
                        if (staff.isEmpty()) System.out.println("No registered drivers or conductors found.");
                        else {
                            System.out.println("\n--- REGISTERED DRIVERS & CONDUCTORS ---");
                            staff.forEach(System.out::println);
                        }
                    } else if (uChoice.equals("5")) {
                        List<User> staff = userDao.getStaffUsers();
                        if (staff.isEmpty()) System.out.println("No staff accounts available to delete.");
                        else {
                            staff.forEach(System.out::println);
                            System.out.print("Enter Driver/Conductor User ID to delete: ");
                            try {
                                int id = Integer.parseInt(sc.nextLine().trim());
                                if (userDao.deleteStaffUser(id)) System.out.println("[SUCCESS] Staff account deleted!");
                                else System.out.println("[!] Invalid ID or target is not a driver/conductor.");
                            } catch (NumberFormatException e) { System.out.println("Invalid ID format!"); }
                        }
                    } else if (uChoice.equals("6")) {
                        managingUsers = false;
                    }
                }
            } else if (choice.equals("2")) {
                boolean managingFleet = true;
                while (managingFleet) {
                    System.out.println("\n--- FLEET & OPERATIONS ---");
                    System.out.println("1. View Full Passenger Safety Log");
                    System.out.println("2. View Vehicle Maintenance Checklist");
                    System.out.println("3. Back to Main Admin Menu");
                    System.out.print("Select option (1-3): ");
                    String fChoice = sc.nextLine().trim();

                    if (fChoice.equals("1")) {
                        List<Passenger> allPassengers = passengerDao.getAllPassengersHistory();
                        if (allPassengers.isEmpty()) System.out.println("No passenger records found.");
                        else {
                            System.out.println("\n==========================================================================");
                            System.out.println("           PASSENGER SAFETY AUDIT LOG (DISEMBARKED & ACTIVE)            ");
                            System.out.println("==========================================================================");
                            allPassengers.forEach(p -> System.out.printf("[%d] Time: %s | Name: %s | Dest: %s (%.1f km) | Type: %s | Status: %s\n",
                                    p.getId(), p.getCreatedAt(), p.getName(), p.getDestination(), p.getDistance(), p.getType(), p.getStatus()));
                            System.out.println("==========================================================================");
                        }
                    } else if (fChoice.equals("2")) {
                        List<MaintenanceItem> items = maintenanceDao.getChecklist();
                        System.out.println("\n--- VEHICLE MAINTENANCE STATUS ---");
                        items.forEach(i -> System.out.printf("[%d] %s -> Status: %s\n", i.getId(), i.getItem(), i.getStatus()));
                    } else if (fChoice.equals("3")) {
                        managingFleet = false;
                    }
                }
            } else if (choice.equals("3")) {
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
            } else if (choice.equals("4")) {
                List<FareRequest> requests = passengerDao.getPendingFareRequests();
                if (requests.isEmpty()) {
                    System.out.println("No pending fare increase requests.");
                } else {
                    System.out.println("\n--- PENDING FARE INCREASE REQUESTS ---");
                    requests.forEach(System.out::println);
                    System.out.print("Enter Request ID to process (0 to go back): ");
                    try {
                        int reqId = Integer.parseInt(sc.nextLine().trim());
                        if (reqId > 0) {
                            System.out.print("Action: [1] Approve & Apply Fare | [2] Reject: ");
                            String act = sc.nextLine().trim();
                            boolean approve = act.equals("1");
                            if (passengerDao.processFareRequest(reqId, approve)) {
                                System.out.println(approve ? "[SUCCESS] Fare increase approved and applied system-wide!" : "[SUCCESS] Fare request rejected.");
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input!");
                    }
                }
            } else if (choice.equals("5")) {
                break;
            }
        }
    }

    private static void conductorPanel() {
        while (true) {
            List<Passenger> activePassengers = passengerDao.getActivePassengers();
            int currentSeats = activePassengers.size();
            double[] currentFareConfig = passengerDao.getActiveFare();

            System.out.println("\n--- CONDUCTOR PANEL ---");
            System.out.printf("Current Capacity: %d/%d Seats | Active Rates: Base P%.2f, Extra P%.2f/km\n",
                    currentSeats, MAX_CAPACITY, currentFareConfig[0], currentFareConfig[1]);
            System.out.println("1. Add Passenger & Collect Fare (CREATE)");
            System.out.println("2. View Active Passengers (READ)");
            System.out.println("3. Disembark Passenger Upon Arrival (DELETE Feature 1)");
            System.out.println("4. Reset Trip Seats / Pasada Loop (DELETE Feature 2)");
            System.out.println("5. Logout");
            System.out.print("Select option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    if (currentSeats >= MAX_CAPACITY) {
                        System.out.println("[!] Jeepney is full (18/18 occupied)!");
                        break;
                    }
                    System.out.print("Passenger Name: ");
                    String name = sc.nextLine().trim();
                    System.out.print("Destination / Drop-off Point: ");
                    String dest = sc.nextLine().trim();
                    System.out.print("Distance in km: ");
                    double dist;
                    try {
                        dist = Double.parseDouble(sc.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid distance entry!");
                        break;
                    }

                    System.out.println("Category: [1] Regular | [2] Student | [3] Senior | [4] PWD");
                    System.out.print("Choice: ");
                    String catOpt = sc.nextLine().trim();

                    System.out.print("Date & Time (Press ENTER for current time): ");
                    String customTime = sc.nextLine().trim();
                    if (customTime.isEmpty()) {
                        customTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    }

                    boolean isDiscounted = false;
                    String type = "Regular";
                    if (catOpt.equals("2")) { type = "Student"; isDiscounted = true; }
                    else if (catOpt.equals("3")) { type = "Senior Citizen"; isDiscounted = true; }
                    else if (catOpt.equals("4")) { type = "PWD"; isDiscounted = true; }

                    double baseFare = currentFareConfig[0];
                    double ratePerExtraKm = currentFareConfig[1];
                    double rawFare = (dist <= baseKm) ? baseFare : baseFare + (Math.ceil(dist - baseKm) * ratePerExtraKm);
                    double finalFare = isDiscounted ? (rawFare * (1.0 - discountRate)) : rawFare;

                    Passenger p = new Passenger(name, dest, dist, type, rawFare, finalFare, isDiscounted, "Active", customTime);
                    if (passengerDao.savePassenger(p)) {
                        // Retrieve latest saved active list to get generated Ticket ID
                        List<Passenger> updatedList = passengerDao.getActivePassengers();
                        int ticketId = updatedList.isEmpty() ? 0 : updatedList.get(updatedList.size() - 1).getId();

                        System.out.println("\n[SUCCESS] Ticket saved to database.");
                        printPassengerReceipt(ticketId, p);
                    }
                }
                case "2" -> {
                    if (activePassengers.isEmpty()) {
                        System.out.println("No active passengers on board.");
                    } else {
                        activePassengers.forEach(p -> System.out.printf("[%d] Name: %s | Time: %s | Dest: %s (%.1f km) - %s | Fare: P%.2f\n",
                                p.getId(), p.getName(), p.getCreatedAt(), p.getDestination(), p.getDistance(), p.getType(), p.getFinalFare()));
                    }
                }
                case "3" -> {
                    if (activePassengers.isEmpty()) {
                        System.out.println("No passengers to disembark.");
                        break;
                    }
                    activePassengers.forEach(p -> System.out.printf("[%d] Name: %s | Dest: %s\n", p.getId(), p.getName(), p.getDestination()));
                    System.out.print("Enter Passenger ID who reached destination: ");
                    try {
                        int pid = Integer.parseInt(sc.nextLine().trim());
                        if (passengerDao.disembarkPassenger(pid)) {
                            System.out.println("[SUCCESS] Passenger disembarked. Seat freed!");
                        } else {
                            System.out.println("[!] Invalid Passenger ID.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format!");
                    }
                }
                case "4" -> {
                    passengerDao.resetTripSeats();
                    System.out.println("[SUCCESS] Trip reset! All seats cleared for new Pasada loop.");
                }
                case "5" -> { return; }
            }
        }
    }

    // Helper method to display digital receipt
    private static void printPassengerReceipt(int ticketId, Passenger p) {
        System.out.println("=========================================");
        System.out.println("        TJOMS PASSENGER RECEIPT          ");
        System.out.println("=========================================");
        System.out.printf(" Ticket ID   : #%04d\n", ticketId);
        System.out.printf(" Date / Time : %s\n", p.getCreatedAt());
        System.out.printf(" Passenger   : %s\n", p.getName());
        System.out.printf(" Destination : %s (%.1f km)\n", p.getDestination(), p.getDistance());
        System.out.printf(" Ticket Type : %s\n", p.getType());
        System.out.printf(" Total Fare  : P%.2f %s\n", p.getFinalFare(), p.isDiscounted() ? "(20% Discounted)" : "");
        System.out.println("=========================================");
        System.out.println("   Thank you for riding with us! Safe trip! ");
        System.out.println("=========================================");
    }

    private static void driverPanel(String driverName) {
        while (true) {
            double[] activeFare = passengerDao.getActiveFare();
            System.out.println("\n--- DRIVER PANEL ---");
            System.out.printf("Current Active Rates -> Base Fare: P%.2f | Extra/km: P%.2f\n", activeFare[0], activeFare[1]);
            System.out.println("1. View Daily Financial Summary (REPORT)");
            System.out.println("2. Log Gas/Fuel Expense (CREATE)");
            System.out.println("3. Vehicle Maintenance Checklist (READ/UPDATE)");
            System.out.println("4. Request Fare Increase (Due to Gas Hike)");
            System.out.println("5. Logout");
            System.out.print("Select option (1-5): ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    double gross = passengerDao.getTotalEarnings();
                    double exp = maintenanceDao.getTotalExpenses();
                    double net = gross - exp;

                    System.out.println("\n==================================");
                    System.out.printf("Gross Earnings : P%.2f\n", gross);
                    System.out.printf("Total Expenses : P%.2f\n", exp);
                    System.out.printf("NET TAKE-HOME  : P%.2f\n", net);
                    System.out.println("==================================");
                }
                case "2" -> {
                    System.out.print("Enter fuel expense amount (P): ");
                    try {
                        double amt = Double.parseDouble(sc.nextLine().trim());
                        maintenanceDao.logExpense("FUEL", amt, "Fuel purchase");
                        System.out.println("[SUCCESS] Fuel expense logged.");
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount!");
                    }
                }
                case "3" -> {
                    List<MaintenanceItem> items = maintenanceDao.getChecklist();
                    System.out.println("\n--- MAINTENANCE CHECKLIST ---");
                    items.forEach(i -> System.out.printf("[%d] %s -> Status: %s\n", i.getId(), i.getItem(), i.getStatus()));
                    System.out.print("Select Item ID to update status (or 0 to skip): ");
                    try {
                        int iid = Integer.parseInt(sc.nextLine().trim());
                        if (iid > 0) {
                            System.out.print("Enter new status (Good / Replaced / Needs Check): ");
                            String st = sc.nextLine().trim();
                            maintenanceDao.updateStatus(iid, st);
                            System.out.println("[SUCCESS] Maintenance status updated.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input!");
                    }
                }
                case "4" -> {
                    System.out.print("Enter proposed new Base Fare (P): ");
                    try {
                        double newBase = Double.parseDouble(sc.nextLine().trim());
                        System.out.print("Enter proposed new Rate per Extra KM (P): ");
                        double newRate = Double.parseDouble(sc.nextLine().trim());

                        if (passengerDao.requestFareChange(newBase, newRate, driverName)) {
                            System.out.println("[SUCCESS] Fare increase request submitted! Waiting for Admin approval.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount entered!");
                    }
                }
                case "5" -> { return; }
            }
        }
    }
}