package com.example.conductor_service.dto.output;

public class AdvanceCurrentStopResponse {

    private String routeId;
    private String busNumber;
    private int currentStop;
    private String updatedAt;

    public AdvanceCurrentStopResponse() {
    }

    public AdvanceCurrentStopResponse(String routeId, String busNumber, int currentStop, String updatedAt) {
        this.routeId = routeId;
        this.busNumber = busNumber;
        this.currentStop = currentStop;
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

    public int getCurrentStop() {
        return currentStop;
    }

    public void setCurrentStop(int currentStop) {
        this.currentStop = currentStop;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
