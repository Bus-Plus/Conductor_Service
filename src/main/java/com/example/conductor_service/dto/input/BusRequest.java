package com.example.conductor_service.dto.input;

public class BusRequest {

    private String routeId;
    private String busNumber;

    public BusRequest() {
    }

    public BusRequest(String routeId, String busNumber) {
        this.routeId = routeId;
        this.busNumber = busNumber;
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
}
