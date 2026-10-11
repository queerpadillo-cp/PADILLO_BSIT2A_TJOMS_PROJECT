package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:JeepneyOperations.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL,
                    status TEXT NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS passengers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    passenger_name TEXT NOT NULL,
                    destination TEXT NOT NULL,
                    distance REAL NOT NULL,
                    type TEXT NOT NULL,
                    original_fare REAL NOT NULL,
                    final_fare REAL NOT NULL,
                    is_discounted INTEGER NOT NULL,
                    status TEXT DEFAULT 'Active',
                    created_at TEXT NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS maintenance (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_name TEXT UNIQUE NOT NULL,
                    status TEXT NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    amount REAL NOT NULL,
                    description TEXT
                );
            """);

            // Active fare configuration table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS fare_config (
                    id INTEGER PRIMARY KEY,
                    base_fare REAL NOT NULL,
                    rate_per_km REAL NOT NULL
                );
            """);

            // Seed default active fare if empty
            stmt.execute("""
                INSERT OR IGNORE INTO fare_config (id, base_fare, rate_per_km) 
                VALUES (1, 13.00, 1.80);
            """);

            // Fare change requests table submitted by drivers
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS fare_requests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    base_fare REAL NOT NULL,
                    rate_per_km REAL NOT NULL,
                    status TEXT DEFAULT 'PENDING',
                    requested_by TEXT NOT NULL
                );
            """);

            // Safe column additions for backward compatibility
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "passengers", "passenger_name")) {
                if (!rs.next()) { stmt.execute("ALTER TABLE passengers ADD COLUMN passenger_name TEXT DEFAULT 'Unknown';"); }
            }
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "passengers", "status")) {
                if (!rs.next()) { stmt.execute("ALTER TABLE passengers ADD COLUMN status TEXT DEFAULT 'Active';"); }
            }
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "passengers", "created_at")) {
                if (!rs.next()) { stmt.execute("ALTER TABLE passengers ADD COLUMN created_at TEXT DEFAULT '';"); }
            }

            stmt.execute("""
                INSERT OR IGNORE INTO users (id, username, password, roles, status)
                VALUES (1, 'admin', 'admin123', 'ADMIN', 'APPROVED');
            """);

            stmt.execute("""
                INSERT OR IGNORE INTO maintenance (id, item_name, status) VALUES
                (1, 'Engine Oil Check', 'Good'),
                (2, 'Brake Fluid & Brake Shoes', 'Good'),
                (3, 'Tire Pressure & Tread', 'Needs Check');
            """);

        } catch (SQLException e) {
            System.err.println("Database Initialization Error: " + e.getMessage());
        }
    }
}