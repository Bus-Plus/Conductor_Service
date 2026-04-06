package com.example.conductor_service.dto.output;

public class ConnectionResponse {

    private String connection;
    private String checkedAt;

    public ConnectionResponse() {
    }

    public ConnectionResponse(String connection, String checkedAt) {
        this.connection = connection;
        this.checkedAt = checkedAt;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(String checkedAt) {
        this.checkedAt = checkedAt;
    }
}
