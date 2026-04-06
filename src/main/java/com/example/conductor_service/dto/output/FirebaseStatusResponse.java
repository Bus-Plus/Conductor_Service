package com.example.conductor_service.dto.output;

public class FirebaseStatusResponse {

    private String firebase;
    private String service;
    private String checkedAt;

    public FirebaseStatusResponse() {
    }

    public FirebaseStatusResponse(String firebase, String service, String checkedAt) {
        this.firebase = firebase;
        this.service = service;
        this.checkedAt = checkedAt;
    }

    public String getFirebase() {
        return firebase;
    }

    public void setFirebase(String firebase) {
        this.firebase = firebase;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(String checkedAt) {
        this.checkedAt = checkedAt;
    }
}
