package com.scc.model;

public class MaintenanceItem {
    private int id;
    private String item;
    private String status;

    public MaintenanceItem(int id, String item, String status) {
        this.id = id;
        this.item = item;
        this.status = status;
    }

    public int getId() { return id; }
    public String getItem() { return item; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}