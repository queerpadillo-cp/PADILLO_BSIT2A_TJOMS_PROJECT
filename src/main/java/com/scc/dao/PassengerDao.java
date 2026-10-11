package com.scc.dao;

import com.scc.model.FareRequest;
import com.scc.model.Passenger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerDao {

    // Retrieve active base fare and rate per km from database
    public double[] getActiveFare() {
        double[] fare = {13.00, 1.80}; // Default fallback
        String sql = "SELECT base_fare, rate_per_km FROM fare_config WHERE id = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                fare[0] = rs.getDouble("base_fare");
                fare[1] = rs.getDouble("rate_per_km");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active fare: " + e.getMessage());
        }
        return fare;
    }

    // Driver submits a fare increase request
    public boolean requestFareChange(double baseFare, double ratePerKm, String driverName) {
        String sql = "INSERT INTO fare_requests(base_fare, rate_per_km, status, requested_by) VALUES(?, ?, 'PENDING', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, baseFare);
            pstmt.setDouble(2, ratePerKm);
            pstmt.setString(3, driverName);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error requesting fare change: " + e.getMessage());
            return false;
        }
    }

    // Admin views all pending fare increase requests
    public List<FareRequest> getPendingFareRequests() {
        List<FareRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM fare_requests WHERE status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new FareRequest(
                        rs.getInt("id"),
                        rs.getDouble("base_fare"),
                        rs.getDouble("rate_per_km"),
                        rs.getString("status"),
                        rs.getString("requested_by")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching fare requests: " + e.getMessage());
        }
        return list;
    }

    // Admin approves or rejects fare request
    public boolean processFareRequest(int requestId, boolean approve) {
        String newStatus = approve ? "APPROVED" : "REJECTED";
        String updateReqSql = "UPDATE fare_requests SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtReq = conn.prepareStatement(updateReqSql)) {

            pstmtReq.setString(1, newStatus);
            pstmtReq.setInt(2, requestId);
            pstmtReq.executeUpdate();

            // If approved, update active fare config table
            if (approve) {
                String selectSql = "SELECT base_fare, rate_per_km FROM fare_requests WHERE id = ?";
                try (PreparedStatement pstmtSel = conn.prepareStatement(selectSql)) {
                    pstmtSel.setInt(1, requestId);
                    ResultSet rs = pstmtSel.executeQuery();
                    if (rs.next()) {
                        double newBase = rs.getDouble("base_fare");
                        double newRate = rs.getDouble("rate_per_km");

                        String updateConfigSql = "UPDATE fare_config SET base_fare = ?, rate_per_km = ? WHERE id = 1";
                        try (PreparedStatement pstmtConfig = conn.prepareStatement(updateConfigSql)) {
                            pstmtConfig.setDouble(1, newBase);
                            pstmtConfig.setDouble(2, newRate);
                            pstmtConfig.executeUpdate();
                        }
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error processing fare request: " + e.getMessage());
            return false;
        }
    }

    public boolean savePassenger(Passenger p) {
        String sql = "INSERT INTO passengers(passenger_name, destination, distance, type, original_fare, final_fare, is_discounted, status, created_at) VALUES(?, ?, ?, ?, ?, ?, ?, 'Active', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getName());
            pstmt.setString(2, p.getDestination());
            pstmt.setDouble(3, p.getDistance());
            pstmt.setString(4, p.getType());
            pstmt.setDouble(5, p.getOriginalFare());
            pstmt.setDouble(6, p.getFinalFare());
            pstmt.setInt(7, p.isDiscounted() ? 1 : 0);
            pstmt.setString(8, p.getCreatedAt());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving passenger record: " + e.getMessage());
            return false;
        }
    }

    public List<Passenger> getActivePassengers() {
        List<Passenger> list = new ArrayList<>();
        String sql = "SELECT * FROM passengers WHERE status = 'Active'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                boolean discounted = rs.getInt("is_discounted") == 1;
                list.add(new Passenger(
                        rs.getInt("id"),
                        rs.getString("passenger_name"),
                        rs.getString("destination"),
                        rs.getDouble("distance"),
                        rs.getString("type"),
                        rs.getDouble("original_fare"),
                        rs.getDouble("final_fare"),
                        discounted,
                        rs.getString("status"),
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active passengers: " + e.getMessage());
        }
        return list;
    }

    public List<Passenger> getAllPassengersHistory() {
        List<Passenger> list = new ArrayList<>();
        String sql = "SELECT * FROM passengers ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                boolean discounted = rs.getInt("is_discounted") == 1;
                list.add(new Passenger(
                        rs.getInt("id"),
                        rs.getString("passenger_name"),
                        rs.getString("destination"),
                        rs.getDouble("distance"),
                        rs.getString("type"),
                        rs.getDouble("original_fare"),
                        rs.getDouble("final_fare"),
                        discounted,
                        rs.getString("status"),
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching passenger history: " + e.getMessage());
        }
        return list;
    }

    public boolean disembarkPassenger(int passengerId) {
        String sql = "UPDATE passengers SET status = 'Disembarked' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, passengerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error disembarking passenger: " + e.getMessage());
            return false;
        }
    }

    public void resetTripSeats() {
        String sql = "UPDATE passengers SET status = 'Disembarked' WHERE status = 'Active'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Error resetting trip seats: " + e.getMessage());
        }
    }

    public double getTotalEarnings() {
        String sql = "SELECT SUM(final_fare) FROM passengers WHERE status = 'Active' OR status = 'Disembarked'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error calculating earnings: " + e.getMessage());
        }
        return 0.0;
    }
}