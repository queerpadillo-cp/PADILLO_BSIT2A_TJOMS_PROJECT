package com.scc.dao;

import com.scc.model.MaintenanceItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDao {

    public List<MaintenanceItem> getChecklist() {
        List<MaintenanceItem> list = new ArrayList<>();
        String sql = "SELECT * FROM maintenance";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new MaintenanceItem(
                        rs.getInt("id"),
                        rs.getString("item_name"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading checklist: " + e.getMessage());
        }
        return list;
    }

    public boolean updateStatus(int id, String newStatus) {
        String sql = "UPDATE maintenance SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating status: " + e.getMessage());
            return false;
        }
    }

    public boolean logExpense(String type, double amount, String description) {
        String sql = "INSERT INTO expenses(type, amount, description) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error logging expense: " + e.getMessage());
            return false;
        }
    }

    public double getTotalExpenses() {
        String sql = "SELECT SUM(amount) FROM expenses";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error calculating expenses: " + e.getMessage());
        }
        return 0.0;
    }
}