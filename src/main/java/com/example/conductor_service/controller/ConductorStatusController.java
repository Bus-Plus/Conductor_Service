package com.example.conductor_service.controller;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.firestore.Firestore;

@RestController
@RequestMapping("/conductor")
public class ConductorStatusController {

    private final Firestore firestore;

    public ConductorStatusController(Firestore firestore) {
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
}
