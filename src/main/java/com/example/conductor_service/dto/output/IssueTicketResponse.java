package com.example.conductor_service.dto.output;

public class IssueTicketResponse {

    private String routeId;
    private String busNumber;
    private String destination;
    private String status;
    private String message;
    private String issuedAt;

    public IssueTicketResponse() {
    }

    public IssueTicketResponse(String routeId, String busNumber, String destination, String status, String message,
            String issuedAt) {
        this.routeId = routeId;
        this.busNumber = busNumber;
        this.destination = destination;
        this.status = status;
        this.message = message;
        this.issuedAt = issuedAt;
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

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(String issuedAt) {
        this.issuedAt = issuedAt;
    }
}
