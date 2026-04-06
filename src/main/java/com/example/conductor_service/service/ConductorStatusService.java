package com.example.conductor_service.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.example.conductor_service.repository.ConductorStatusRepository;
import com.google.cloud.firestore.DocumentSnapshot;

@Service
public class ConductorStatusService {

    private final ConductorStatusRepository conductorStatusRepository;

    public ConductorStatusService(ConductorStatusRepository conductorStatusRepository) {
        this.conductorStatusRepository = conductorStatusRepository;
    }

    public int advanceCurrentStop(String routeId, String busNumber)
            throws InterruptedException, ExecutionException {
        DocumentSnapshot snapshot = conductorStatusRepository.findBus(routeId, busNumber);
        if (!snapshot.exists()) {
            throw new BusNotFoundException(routeId, busNumber);
        }

        Long currentStopValue = snapshot.getLong("currentStop");
        int nextStop = currentStopValue == null ? 1 : currentStopValue.intValue() + 1;

        conductorStatusRepository.updateCurrentStop(routeId, busNumber, nextStop);
        return nextStop;
    }

    public Map<String, Object> getBusDetails(String routeId, String busNumber)
            throws InterruptedException, ExecutionException {
        DocumentSnapshot snapshot = conductorStatusRepository.findBus(routeId, busNumber);
        if (!snapshot.exists()) {
            throw new BusNotFoundException(routeId, busNumber);
        }
        Map<String, Object> details = snapshot.getData();
        return details != null ? details : Map.of();
    }

    public int resetTrip(String routeId, String busNumber)
            throws InterruptedException, ExecutionException {
        DocumentSnapshot snapshot = conductorStatusRepository.findBus(routeId, busNumber);
        if (!snapshot.exists()) {
            throw new BusNotFoundException(routeId, busNumber);
        }

        Object stopsValue = snapshot.get("stops");
        int stopCount = getStopCount(stopsValue);
        boolean stringValues = isStringStops(stopsValue);

        conductorStatusRepository.resetTrip(routeId, busNumber, stopCount, stringValues);
        return stopCount;
    }

    private int getStopCount(Object stopsValue) {
        if (stopsValue instanceof List) {
            return ((List<?>) stopsValue).size();
        }
        if (stopsValue instanceof String stringValue) {
            return countStopsInString(stringValue);
        }
        return 0;
    }

    private boolean isStringStops(Object stopsValue) {
        if (stopsValue instanceof List) {
            return ((List<?>) stopsValue).stream().allMatch(item -> item instanceof String);
        }
        return stopsValue instanceof String;
    }

    private int countStopsInString(String stopsValue) {
        String trimmed = stopsValue.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if (trimmed.isEmpty()) {
            return 0;
        }
        return (int) Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .count();
    }

    public static class BusNotFoundException extends RuntimeException {
        public BusNotFoundException(String routeId, String busNumber) {
            super(String.format("Bus document not found for routeId='%s', busNumber='%s'", routeId, busNumber));
        }
    }
}
