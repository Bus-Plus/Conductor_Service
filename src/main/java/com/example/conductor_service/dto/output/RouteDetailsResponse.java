package com.example.conductor_service.dto.output;

import java.util.Map;

public class RouteDetailsResponse {

    private String routeId;
    private Map<String, Object> details;

    public RouteDetailsResponse() {
    }

    public RouteDetailsResponse(String routeId, Map<String, Object> details) {
        this.routeId = routeId;
        this.details = details;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
