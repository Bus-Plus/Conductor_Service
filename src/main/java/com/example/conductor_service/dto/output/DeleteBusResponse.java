package com.example.conductor_service.dto.output;

public class DeleteBusResponse {

    private String routeId;
    private String busNumber;
    private boolean deleted;
    private String updatedAt;

    public DeleteBusResponse() {
    }

    public DeleteBusResponse(String routeId, String busNumber, boolean deleted, String updatedAt) {
        this.routeId = routeId;
        this.busNumber = busNumber;
        this.deleted = deleted;
        this.updatedAt = updatedAt;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
