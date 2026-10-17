package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditDao {
    public static void logEvent(int userId, String eventType, String description) {
        String sql = "INSERT INTO audit_logs (user_id, event_type, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, eventType);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving audit log: " + e.getMessage());
        }
    }
}