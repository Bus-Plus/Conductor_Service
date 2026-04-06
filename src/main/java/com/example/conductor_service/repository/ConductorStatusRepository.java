package com.example.conductor_service.repository;

import java.util.Collections;
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
}
