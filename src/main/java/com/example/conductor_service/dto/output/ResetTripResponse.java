package com.example.conductor_service.dto.output;

public class ResetTripResponse {

    private String routeId;
    private String busNumber;
    private int currentStop;
    private int stopsCount;
    private String updatedAt;

    public ResetTripResponse() {
    }

    public ResetTripResponse(String routeId, String busNumber, int currentStop, int stopsCount, String updatedAt) {
        this.routeId = routeId;
        this.busNumber = busNumber;
        this.currentStop = currentStop;
        this.stopsCount = stopsCount;
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

    public int getStopsCount() {
        return stopsCount;
    }

    public void setStopsCount(int stopsCount) {
        this.stopsCount = stopsCount;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
