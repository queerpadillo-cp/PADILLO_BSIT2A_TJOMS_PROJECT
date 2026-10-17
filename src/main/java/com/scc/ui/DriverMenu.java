package com.scc.ui;

import com.scc.dao.MaintenanceDao;
import com.scc.dao.PassengerDao;
import com.scc.model.MaintenanceItem;

import java.util.List;
import java.util.Scanner;

public class DriverMenu {
    private final PassengerDao passengerDao;
    private final MaintenanceDao maintenanceDao;
    private final Scanner scanner;
    private final String driverName;

    public DriverMenu(PassengerDao passengerDao, MaintenanceDao maintenanceDao, Scanner scanner, String driverName) {
        this.passengerDao = passengerDao;
        this.maintenanceDao = maintenanceDao;
        this.scanner = scanner;
        this.driverName = driverName;
    }

    public void displayMenu() {
        while (true) {
            double[] activeFare = passengerDao.getActiveFare();
            System.out.println("\n--- DRIVER PANEL ---");
            System.out.printf("Active Rates -> Base: P%.2f | Extra/km: P%.2f\n", activeFare[0], activeFare[1]);
            System.out.println("1. View Daily Financial Summary");
            System.out.println("2. Log Gas/Fuel Expense");
            System.out.println("3. Vehicle Maintenance Checklist");
            System.out.println("4. Request Fare Increase");
            System.out.println("5. Logout");
            System.out.print("Select option (1-5): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    double gross = passengerDao.getTotalEarnings();
                    double exp = maintenanceDao.getTotalExpenses();
                    System.out.printf("\nGross: P%.2f | Expenses: P%.2f | Net: P%.2f\n", gross, exp, (gross - exp));
                }
                case "2" -> {
                    System.out.print("Enter fuel expense amount (P): ");
                    double amt = Double.parseDouble(scanner.nextLine().trim());
                    maintenanceDao.logExpense("FUEL", amt, "Fuel purchase");
                    System.out.println("[SUCCESS] Expense logged.");
                }
                case "3" -> {
                    List<MaintenanceItem> items = maintenanceDao.getChecklist();
                    items.forEach(i -> System.out.printf("[%d] %s -> Status: %s\n", i.getId(), i.getItem(), i.getStatus()));
                    System.out.print("Select Item ID to update (0 to skip): ");
                    int iid = Integer.parseInt(scanner.nextLine().trim());
                    if (iid > 0) {
                        System.out.print("Enter new status (Good / Replaced / Needs Check): ");
                        maintenanceDao.updateStatus(iid, scanner.nextLine().trim());
                        System.out.println("[SUCCESS] Updated.");
                    }
                }
                case "4" -> {
                    System.out.print("New Base Fare (P): ");
                    double newBase = Double.parseDouble(scanner.nextLine().trim());
                    System.out.print("New Rate per Extra KM (P): ");
                    double newRate = Double.parseDouble(scanner.nextLine().trim());
                    passengerDao.requestFareChange(newBase, newRate, driverName);
                    System.out.println("[SUCCESS] Fare request submitted to Admin.");
                }
                case "5" -> { return; }
            }
        }
    }
}