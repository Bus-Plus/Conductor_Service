package com.example.conductor_service.dto.output;

import java.util.Map;

public class BusDetailsResponse {

    private String routeId;
    private String busNumber;
    private Map<String, Object> details;

    public BusDetailsResponse() {
    }

    public BusDetailsResponse(String routeId, String busNumber, Map<String, Object> details) {
        this.routeId = routeId;
        this.busNumber = busNumber;
        this.details = details;
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

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
