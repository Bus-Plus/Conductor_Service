package com.example.conductor_service.controller;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.conductor_service.service.ConductorStatusService;
import com.example.conductor_service.service.ConductorStatusService.BusNotFoundException;
import com.google.cloud.firestore.Firestore;

@RestController
@RequestMapping("/conductor")
public class ConductorStatusController {

    private final ConductorStatusService conductorStatusService;
    private final Firestore firestore;

    public ConductorStatusController(ConductorStatusService conductorStatusService, Firestore firestore) {
        this.conductorStatusService = conductorStatusService;
        this.firestore = firestore;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean authenticated = SecurityContextHolder.getContext().getAuthentication() != null;
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "service", "conductor_service",
                "authenticated", authenticated
        ));
    }

    @GetMapping("/connection")
    public ResponseEntity<Map<String, Object>> getConnection() {
        return ResponseEntity.ok(Map.of(
                "connection", "active",
                "checkedAt", Instant.now().toString()
        ));
    }

    @GetMapping("/firebase/status")
    public ResponseEntity<Map<String, Object>> getFirebaseStatus() {
        try {
            firestore.collection("health_check").limit(1).get().get();
            return ResponseEntity.ok(Map.of(
                    "firebase", "connected",
                    "service", "conductor_service",
                    "checkedAt", Instant.now().toString()
            ));
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(Map.of(
                    "firebase", "unavailable",
                    "error", ex.getMessage(),
                    "service", "conductor_service"
            ));
        }
    }

    @PutMapping("/bus/{routeId}/{busNumber}/current-stop/advance")
    public ResponseEntity<Map<String, Object>> advanceCurrentStop(
            @PathVariable String routeId,
            @PathVariable String busNumber) {

        try {
            int nextStop = conductorStatusService.advanceCurrentStop(routeId, busNumber);

            return ResponseEntity.ok(Map.of(
                    "routeId", routeId,
                    "busNumber", busNumber,
                    "currentStop", nextStop,
                    "updatedAt", Instant.now().toString()
            ));
        } catch (BusNotFoundException ex) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", ex.getMessage(),
                    "routeId", routeId,
                    "busNumber", busNumber
            ));
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Failed to update currentStop",
                    "message", ex.getMessage(),
                    "routeId", routeId,
                    "busNumber", busNumber
            ));
        }
    }

    @PutMapping("/bus/{routeId}/{busNumber}/reset")
    public ResponseEntity<Map<String, Object>> resetTrip(
            @PathVariable String routeId,
            @PathVariable String busNumber) {

        try {
            int stopCount = conductorStatusService.resetTrip(routeId, busNumber);

            return ResponseEntity.ok(Map.of(
                    "routeId", routeId,
                    "busNumber", busNumber,
                    "currentStop", 0,
                    "stopsCount", stopCount,
                    "updatedAt", Instant.now().toString()
            ));
        } catch (BusNotFoundException ex) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", ex.getMessage(),
                    "routeId", routeId,
                    "busNumber", busNumber
            ));
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Failed to reset trip",
                    "message", ex.getMessage(),
                    "routeId", routeId,
                    "busNumber", busNumber
            ));
        }
    }

    @GetMapping("/bus/{routeId}/{busNumber}")
    public ResponseEntity<Map<String, Object>> getBusDetails(
            @PathVariable String routeId,
            @PathVariable String busNumber) {

        try {
            Map<String, Object> details = conductorStatusService.getBusDetails(routeId, busNumber);
            return ResponseEntity.ok(details);
        } catch (BusNotFoundException ex) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", ex.getMessage(),
                    "routeId", routeId,
                    "busNumber", busNumber
            ));
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Failed to fetch bus details",
                    "message", ex.getMessage(),
                    "routeId", routeId,
                    "busNumber", busNumber
            ));
        }
    }
}
