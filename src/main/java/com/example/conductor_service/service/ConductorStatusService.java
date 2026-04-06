package com.example.conductor_service.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.example.conductor_service.dto.input.BusRequest;
import com.example.conductor_service.dto.output.AdvanceCurrentStopResponse;
import com.example.conductor_service.dto.output.BusDetailsResponse;
import com.example.conductor_service.dto.output.DeleteBusResponse;
import com.example.conductor_service.dto.output.DocumentNamesResponse;
import com.example.conductor_service.dto.output.ResetTripResponse;
import com.example.conductor_service.repository.ConductorStatusRepository;
import com.google.cloud.firestore.DocumentSnapshot;

@Service
public class ConductorStatusService {

    private final ConductorStatusRepository conductorStatusRepository;

    public ConductorStatusService(ConductorStatusRepository conductorStatusRepository) {
        this.conductorStatusRepository = conductorStatusRepository;
    }

    public AdvanceCurrentStopResponse advanceCurrentStop(BusRequest request)
            throws InterruptedException, ExecutionException {
        DocumentSnapshot snapshot = conductorStatusRepository.findBus(request.getRouteId(), request.getBusNumber());
        if (!snapshot.exists()) {
            throw new BusNotFoundException(request.getRouteId(), request.getBusNumber());
        }

        Long currentStopValue = snapshot.getLong("currentStop");
        int nextStop = currentStopValue == null ? 1 : currentStopValue.intValue() + 1;

        conductorStatusRepository.updateCurrentStop(request.getRouteId(), request.getBusNumber(), nextStop);
        return new AdvanceCurrentStopResponse(request.getRouteId(), request.getBusNumber(), nextStop, java.time.Instant.now().toString());
    }

    public BusDetailsResponse getBusDetails(BusRequest request)
            throws InterruptedException, ExecutionException {
        DocumentSnapshot snapshot = conductorStatusRepository.findBus(request.getRouteId(), request.getBusNumber());
        if (!snapshot.exists()) {
            throw new BusNotFoundException(request.getRouteId(), request.getBusNumber());
        }
        Map<String, Object> details = snapshot.getData();
        return new BusDetailsResponse(request.getRouteId(), request.getBusNumber(), details != null ? details : Map.of());
    }

    public ResetTripResponse resetTrip(BusRequest request)
            throws InterruptedException, ExecutionException {
        DocumentSnapshot snapshot = conductorStatusRepository.findBus(request.getRouteId(), request.getBusNumber());
        if (!snapshot.exists()) {
            throw new BusNotFoundException(request.getRouteId(), request.getBusNumber());
        }

        Object stopsValue = snapshot.get("stops");
        int stopCount = getStopCount(stopsValue);
        boolean stringValues = isStringStops(stopsValue);

        conductorStatusRepository.resetTrip(request.getRouteId(), request.getBusNumber(), stopCount, stringValues);
        return new ResetTripResponse(request.getRouteId(), request.getBusNumber(), 0, stopCount, java.time.Instant.now().toString());
    }

    public DeleteBusResponse deleteBus(BusRequest request)
            throws InterruptedException, ExecutionException {
        DocumentSnapshot snapshot = conductorStatusRepository.findBus(request.getRouteId(), request.getBusNumber());
        if (!snapshot.exists()) {
            throw new BusNotFoundException(request.getRouteId(), request.getBusNumber());
        }

        conductorStatusRepository.deleteBus(request.getRouteId(), request.getBusNumber());
        return new DeleteBusResponse(request.getRouteId(), request.getBusNumber(), true, java.time.Instant.now().toString());
    }

    public DocumentNamesResponse listDocumentNames(String collectionId)
            throws InterruptedException, ExecutionException {
        return new DocumentNamesResponse(collectionId, conductorStatusRepository.listDocumentNames(collectionId));
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
