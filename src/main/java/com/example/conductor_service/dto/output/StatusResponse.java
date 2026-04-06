package com.example.conductor_service.dto.output;

public class StatusResponse {

    private String status;
    private String service;
    private boolean authenticated;

    public StatusResponse() {
    }

    public StatusResponse(String status, String service, boolean authenticated) {
        this.status = status;
        this.service = service;
        this.authenticated = authenticated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
