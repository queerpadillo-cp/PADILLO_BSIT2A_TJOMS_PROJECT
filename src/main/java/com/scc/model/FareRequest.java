package com.scc.model;

public record FareRequest(int id, double baseFare, double ratePerKm, String status, String requestedBy) {
    @Override
    public String toString() {
        return String.format("[%d] Base Fare: P%.2f | Extra/km: P%.2f | Requested By: %s | Status: %s",
                id, baseFare, ratePerKm, requestedBy, status);
    }
}