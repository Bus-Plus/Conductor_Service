package com.example.conductor_service.dto.output;

import java.util.List;

public class DocumentNamesResponse {

    private String collectionId;
    private List<String> documentNames;

    public DocumentNamesResponse() {
    }

    public DocumentNamesResponse(String collectionId, List<String> documentNames) {
        this.collectionId = collectionId;
        this.documentNames = documentNames;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public List<String> getDocumentNames() {
        return documentNames;
    }

    public void setDocumentNames(List<String> documentNames) {
        this.documentNames = documentNames;
    }
}
