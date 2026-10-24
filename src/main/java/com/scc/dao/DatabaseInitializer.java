package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initializeTables() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

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
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    event_type TEXT NOT NULL,
                    description TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS passengers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    destination TEXT NOT NULL,
                    distance REAL NOT NULL,
                    type TEXT NOT NULL,
                    original_fare REAL NOT NULL,
                    final_fare REAL NOT NULL,
                    is_discounted INTEGER NOT NULL,
                    status TEXT DEFAULT 'Active',
                    passenger_name TEXT DEFAULT 'Unknown',
                    created_at TEXT DEFAULT ''
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

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS fare_config (
                    id INTEGER PRIMARY KEY,
                    base_fare REAL NOT NULL,
                    rate_per_km REAL NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS fare_requests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    base_fare REAL NOT NULL,
                    rate_per_km REAL NOT NULL,
                    status TEXT DEFAULT 'PENDING',
                    requested_by TEXT NOT NULL
                );
            """);

            System.out.println("[INFO] Database tables initialized successfully.");
        } catch (SQLException e) {
            System.err.println("[!] Error initializing database tables: " + e.getMessage());
        }
    }
}