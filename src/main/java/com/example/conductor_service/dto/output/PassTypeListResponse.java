package com.example.conductor_service.dto.output;

import java.util.List;

public class PassTypeListResponse {

    private List<String> passTypes;

    public PassTypeListResponse() {
    }

    public PassTypeListResponse(List<String> passTypes) {
        this.passTypes = passTypes;
    }

    public List<String> getPassTypes() {
        return passTypes;
    }

    public void setPassTypes(List<String> passTypes) {
        this.passTypes = passTypes;
    }
}
