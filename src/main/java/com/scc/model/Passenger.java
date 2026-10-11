package com.scc.model;

public class Passenger {
    private int id;
    private String name;
    private String destination;
    private double distance;
    private String type;
    private double originalFare;
    private double finalFare;
    private boolean isDiscounted;
    private String status;
    private String createdAt;

    public Passenger(String name, String destination, double distance, String type, double originalFare, double finalFare, boolean isDiscounted, String status, String createdAt) {
        this.name = name;
        this.destination = destination;
        this.distance = distance;
        this.type = type;
        this.originalFare = originalFare;
        this.finalFare = finalFare;
        this.isDiscounted = isDiscounted;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Passenger(int id, String name, String destination, double distance, String type, double originalFare, double finalFare, boolean isDiscounted, String status, String createdAt) {
        this(name, destination, distance, type, originalFare, finalFare, isDiscounted, status, createdAt);
        this.id = id;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDestination() { return destination; }
    public double getDistance() { return distance; }
    public String getType() { return type; }
    public double getOriginalFare() { return originalFare; }
    public double getFinalFare() { return finalFare; }
    public boolean isDiscounted() { return isDiscounted; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}