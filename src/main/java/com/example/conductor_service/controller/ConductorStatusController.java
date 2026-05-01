package com.example.conductor_service.controller;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.conductor_service.dto.input.BusRequest;
import com.example.conductor_service.dto.input.IssueTicketRequest;
import com.example.conductor_service.dto.output.AdvanceCurrentStopResponse;
import com.example.conductor_service.dto.output.BusDetailsResponse;
import com.example.conductor_service.dto.output.ConnectionResponse;
import com.example.conductor_service.dto.output.DeleteBusResponse;
import com.example.conductor_service.dto.output.DocumentNamesResponse;
import com.example.conductor_service.dto.output.FirebaseStatusResponse;
import com.example.conductor_service.dto.output.IssueTicketResponse;
import com.example.conductor_service.dto.output.PassTypeListResponse;
import com.example.conductor_service.dto.output.ResetTripResponse;
import com.example.conductor_service.dto.output.RouteDetailsResponse;
import com.example.conductor_service.dto.output.StatusResponse;
import com.example.conductor_service.service.ConductorStatusService;
import com.example.conductor_service.service.ConductorStatusService.BusNotFoundException;
import com.example.conductor_service.service.ConductorStatusService.RouteNotFoundException;
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
    public ResponseEntity<StatusResponse> getStatus() {
        boolean authenticated = SecurityContextHolder.getContext().getAuthentication() != null;
        StatusResponse response = new StatusResponse("OK", "conductor_service", authenticated);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/connection")
    public ResponseEntity<ConnectionResponse> getConnection() {
        ConnectionResponse response = new ConnectionResponse("active", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/firebase/status")
    public ResponseEntity<FirebaseStatusResponse> getFirebaseStatus() {
        try {
            firestore.collection("health_check").limit(1).get().get();
            FirebaseStatusResponse response = new FirebaseStatusResponse("connected", "conductor_service", Instant.now().toString());
            return ResponseEntity.ok(response);
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(null);
        }
    }

    @PutMapping("/bus/{routeId}/{busNumber}/current-stop/advance")
    public ResponseEntity<AdvanceCurrentStopResponse> advanceCurrentStop(
            @PathVariable String routeId,
            @PathVariable String busNumber) {

        try {
            BusRequest request = new BusRequest(routeId, busNumber);
            AdvanceCurrentStopResponse response = conductorStatusService.advanceCurrentStop(request);
            return ResponseEntity.ok(response);
        } catch (BusNotFoundException ex) {
            return ResponseEntity.status(404).body(null);
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(null);
        }
    }

    @PutMapping("/bus/{routeId}/{busNumber}/reset")
    public ResponseEntity<ResetTripResponse> resetTrip(
            @PathVariable String routeId,
            @PathVariable String busNumber) {

        try {
            BusRequest request = new BusRequest(routeId, busNumber);
            ResetTripResponse response = conductorStatusService.resetTrip(request);
            return ResponseEntity.ok(response);
        } catch (BusNotFoundException ex) {
            return ResponseEntity.status(404).body(null);
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(null);
        }
    }

    @GetMapping("/bus/{routeId}/{busNumber}")
    public ResponseEntity<BusDetailsResponse> getBusDetails(
            @PathVariable String routeId,
            @PathVariable String busNumber) {

        try {
            BusRequest request = new BusRequest(routeId, busNumber);
            BusDetailsResponse response = conductorStatusService.getBusDetails(request);
            return ResponseEntity.ok(response);
        } catch (BusNotFoundException ex) {
            return ResponseEntity.status(404).body(null);
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(null);
        }
    }

    @GetMapping("/bus/{routeId}/{busNumber}/exists")
    public ResponseEntity<Map<String, Object>> doesBusExist(
            @PathVariable String routeId,
            @PathVariable String busNumber) {
        try {
            BusDetailsResponse response = conductorStatusService.getBusDetails(new BusRequest(routeId, busNumber));
            return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "data", response.getDetails()));
        } catch (BusNotFoundException ex) {
            return ResponseEntity.status(404).body(Map.of("exists", false, "data", Map.of()));
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(null);
        }
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<RouteDetailsResponse> getRouteDetails(
            @PathVariable String routeId) {
        try {
            RouteDetailsResponse response = conductorStatusService.getRouteDetails(routeId);
            return ResponseEntity.ok(response);
        } catch (RouteNotFoundException ex) {
            return ResponseEntity.status(404).body(null);
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(null);
        }
    }

    @PostMapping("/bus/{routeId}/{busNumber}/issue-ticket")
    public ResponseEntity<IssueTicketResponse> issueTicket(
            @PathVariable String routeId,
            @PathVariable String busNumber,
            @RequestBody IssueTicketRequest request) {
        try {
            IssueTicketResponse response = conductorStatusService.issueTicket(
                    routeId,
                    busNumber,
                    request.getPaymentMethod(),
                    request.getPassType(),
                    request.getUserId(),
                    request.getDestination(),
                    request.getStops());
            return ResponseEntity.ok(response);
        } catch (BusNotFoundException ex) {
            return ResponseEntity.status(404).body(null);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new IssueTicketResponse(routeId, busNumber, request.getDestination(), "failed", ex.getMessage(), java.time.Instant.now().toString()));
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(null);
        }
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<DocumentNamesResponse> getPaymentMethodDocumentNames() {
        try {
            DocumentNamesResponse response = conductorStatusService.listDocumentNames("payment_methods");
            return ResponseEntity.ok(response);
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(null);
        }
    }

    @GetMapping("/pass-types")
    public ResponseEntity<PassTypeListResponse> getAllPassTypes() {
        PassTypeListResponse response = conductorStatusService.getAllPassTypes();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/bus/{routeId}/{busNumber}")
    public ResponseEntity<DeleteBusResponse> deleteBus(
            @PathVariable String routeId,
            @PathVariable String busNumber) {

        try {
            BusRequest request = new BusRequest(routeId, busNumber);
            DeleteBusResponse response = conductorStatusService.deleteBus(request);
            return ResponseEntity.ok(response);
        } catch (BusNotFoundException ex) {
            return ResponseEntity.status(404).body(null);
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(503).body(null);
        }
    }
}