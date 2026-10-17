package com.scc.ui;

import com.scc.dao.PassengerDao;
import com.scc.model.Passenger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ConductorMenu {
    private final PassengerDao passengerDao;
    private final Scanner scanner;
    private static final int MAX_CAPACITY = 18;
    private static final double baseKm = 4.0;
    private static final double discountRate = 0.20;

    public ConductorMenu(PassengerDao passengerDao, Scanner scanner) {
        this.passengerDao = passengerDao;
        this.scanner = scanner;
    }

    public void displayMenu() {
        while (true) {
            List<Passenger> activePassengers = passengerDao.getActivePassengers();
            int currentSeats = activePassengers.size();
            double[] currentFareConfig = passengerDao.getActiveFare();

            System.out.println("\n--- CONDUCTOR PANEL ---");
            System.out.printf("Capacity: %d/%d Seats | Rates: Base P%.2f, Extra P%.2f/km\n",
                    currentSeats, MAX_CAPACITY, currentFareConfig[0], currentFareConfig[1]);
            System.out.println("1. Add Passenger & Collect Fare (CREATE)");
            System.out.println("2. View Active Passengers (READ)");
            System.out.println("3. Disembark Passenger Upon Arrival");
            System.out.println("4. Reset Trip Seats / Pasada Loop");
            System.out.println("5. Logout");
            System.out.print("Select option (1-5): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    if (currentSeats >= MAX_CAPACITY) {
                        System.out.println("[!] Jeepney is full (18/18 occupied)!");
                        break;
                    }
                    System.out.print("Passenger Name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Destination: ");
                    String dest = scanner.nextLine().trim();
                    System.out.print("Distance in km: ");
                    double dist = Double.parseDouble(scanner.nextLine().trim());

                    System.out.println("Category: [1] Regular | [2] Student | [3] Senior | [4] PWD");
                    String catOpt = scanner.nextLine().trim();

                    System.out.print("Date & Time (Press ENTER for current time): ");
                    String customTime = scanner.nextLine().trim();
                    if (customTime.isEmpty()) {
                        customTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    }

                    boolean isDiscounted = !catOpt.equals("1");
                    String type = switch (catOpt) {
                        case "2" -> "Student";
                        case "3" -> "Senior Citizen";
                        case "4" -> "PWD";
                        default -> "Regular";
                    };

                    double baseFare = currentFareConfig[0];
                    double ratePerExtraKm = currentFareConfig[1];
                    double rawFare = (dist <= baseKm) ? baseFare : baseFare + (Math.ceil(dist - baseKm) * ratePerExtraKm);
                    double finalFare = isDiscounted ? (rawFare * (1.0 - discountRate)) : rawFare;

                    Passenger p = new Passenger(name, dest, dist, type, rawFare, finalFare, isDiscounted, "Active", customTime);
                    if (passengerDao.savePassenger(p)) {
                        List<Passenger> updatedList = passengerDao.getActivePassengers();
                        int ticketId = updatedList.isEmpty() ? 0 : updatedList.get(updatedList.size() - 1).getId();
                        printReceipt(ticketId, p);
                    }
                }
                case "2" -> activePassengers.forEach(p -> System.out.printf("[%d] Name: %s | Dest: %s (%.1f km) | Fare: P%.2f\n",
                        p.getId(), p.getName(), p.getDestination(), p.getDistance(), p.getFinalFare()));
                case "3" -> {
                    activePassengers.forEach(p -> System.out.printf("[%d] Name: %s\n", p.getId(), p.getName()));
                    System.out.print("Enter Passenger ID to disembark: ");
                    int pid = Integer.parseInt(scanner.nextLine().trim());
                    passengerDao.disembarkPassenger(pid);
                    System.out.println("[SUCCESS] Passenger disembarked.");
                }
                case "4" -> {
                    passengerDao.resetTripSeats();
                    System.out.println("[SUCCESS] Trip reset! Seats cleared.");
                }
                case "5" -> { return; }
            }
        }
    }

    private void printReceipt(int ticketId, Passenger p) {
        System.out.println("=========================================");
        System.out.println("        TJOMS PASSENGER RECEIPT          ");
        System.out.println("=========================================");
        System.out.printf(" Ticket ID   : #%04d\n", ticketId);
        System.out.printf(" Date / Time : %s\n", p.getCreatedAt());
        System.out.printf(" Passenger   : %s\n", p.getName());
        System.out.printf(" Destination : %s (%.1f km)\n", p.getDestination(), p.getDistance());
        System.out.printf(" Total Fare  : P%.2f\n", p.getFinalFare());
        System.out.println("=========================================");
    }
}