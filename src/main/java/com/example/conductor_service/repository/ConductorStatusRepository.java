package com.example.conductor_service.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;

@Repository
public class ConductorStatusRepository {

    private final Firestore firestore;

    public ConductorStatusRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public DocumentSnapshot findBus(String routeId, String busNumber)
            throws InterruptedException, ExecutionException {
        DocumentReference busDoc = firestore
                .collection("bus_status")
                .document(routeId)
                .collection("busses")
                .document(busNumber);

        return busDoc.get().get();
    }

    public DocumentSnapshot findRoute(String routeId)
            throws InterruptedException, ExecutionException {
        return firestore
                .collection("bus_stops")
                .document(routeId)
                .get()
                .get();
    }

    public java.util.List<String> findRouteStops(String routeId)
            throws InterruptedException, ExecutionException {
        DocumentSnapshot routeDoc = findRoute(routeId);
        if (!routeDoc.exists()) {
            return java.util.Collections.emptyList();
        }

        Object stopsValue = routeDoc.get("routeStops");
        if (stopsValue == null) {
            stopsValue = routeDoc.get("stops");
        }

        if (stopsValue instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) stopsValue;
            java.util.List<String> stops = new java.util.ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    stops.add(item.toString());
                }
            }
            return stops;
        }

        if (stopsValue instanceof String) {
            String value = ((String) stopsValue).trim();
            if (value.startsWith("[") && value.endsWith("]")) {
                value = value.substring(1, value.length() - 1);
            }
            if (value.isBlank()) {
                return java.util.Collections.emptyList();
            }
            String[] parts = value.split(",");
            java.util.List<String> stops = new java.util.ArrayList<>();
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isBlank()) {
                    stops.add(trimmed);
                }
            }
            return stops;
        }

        return java.util.Collections.emptyList();
    }

    public void updateCurrentStop(String routeId, String busNumber, int currentStop)
            throws InterruptedException, ExecutionException {
        DocumentReference busDoc = firestore
                .collection("bus_status")
                .document(routeId)
                .collection("busses")
                .document(busNumber);

        busDoc.update("currentStop", currentStop).get();
    }

    public void resetTrip(String routeId, String busNumber, int stopCount)
            throws InterruptedException, ExecutionException {
        resetTrip(routeId, busNumber, stopCount, false);
    }

    public void resetTrip(String routeId, String busNumber, int stopCount, boolean stringValues)
            throws InterruptedException, ExecutionException {
        DocumentReference busDoc = firestore
                .collection("bus_status")
                .document(routeId)
                .collection("busses")
                .document(busNumber);

        Object zeroValue = stringValues ? "0" : 0;
        Map<String, Object> resetState = Map.of(
                "currentStop", 0,
                "stops", Collections.nCopies(stopCount, zeroValue)
        );

        busDoc.set(resetState, SetOptions.merge()).get();
    }

    public void deleteBus(String routeId, String busNumber)
            throws InterruptedException, ExecutionException {
        DocumentReference busDoc = firestore
                .collection("bus_status")
                .document(routeId)
                .collection("busses")
                .document(busNumber);

        busDoc.delete().get();
    }

    public void updateStops(String routeId, String busNumber, Object stops)
            throws InterruptedException, ExecutionException {
        DocumentReference busDoc = firestore
                .collection("bus_status")
                .document(routeId)
                .collection("busses")
                .document(busNumber);

        busDoc.update("stops", stops).get();
    }

    public List<String> listDocumentNames(String collectionId)
            throws InterruptedException, ExecutionException {
        List<String> documentNames = new ArrayList<>();
        for (DocumentReference documentReference : firestore.collection(collectionId).listDocuments()) {
            documentNames.add(documentReference.getId());
        }
        return documentNames;
    }
}
