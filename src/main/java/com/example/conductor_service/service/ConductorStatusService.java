package com.example.conductor_service.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.example.conductor_service.dto.input.BusRequest;
import com.example.conductor_service.dto.output.AdvanceCurrentStopResponse;
import com.example.conductor_service.dto.output.BusDetailsResponse;
import com.example.conductor_service.dto.output.DeleteBusResponse;
import com.example.conductor_service.dto.output.DocumentNamesResponse;
import com.example.conductor_service.dto.output.IssueTicketResponse;
import com.example.conductor_service.dto.output.PassTypeListResponse;
import com.example.conductor_service.dto.output.ResetTripResponse;
import com.example.conductor_service.dto.output.RouteDetailsResponse;
import com.example.conductor_service.model.Pass;
import com.example.conductor_service.model.PassType;
import com.example.conductor_service.repository.ConductorStatusRepository;
import com.example.conductor_service.repository.PassRepository;
import com.example.conductor_service.repository.PassTypeRepository;
import com.google.cloud.firestore.DocumentSnapshot;

@Service
public class ConductorStatusService {

    private static final Set<String> ALLOWED_PAYMENT_METHODS = Set.of(
            "gpay", "card", "cash", "passes", "smartcard");
    private static final Set<String> ALLOWED_PASS_TYPES = Set.of(
            "all india service pass",
            "college student concession pass",
            "differently-abled pass",
            "free student pass",
            "govt. employee travel pass",
            "monthly season ticket",
            "point-to-point monthly pass",
            "senior citizen concession pass",
            "weekly pass");
    private static final Set<String> PASS_TYPES_WITH_ROUTE_STOPS = Set.of(
            "point-to-point monthly pass",
            "college student concession pass",
            "free student pass");

    private final ConductorStatusRepository conductorStatusRepository;
    private final PassTypeRepository passTypeRepository;
    private final PassRepository passRepository;

    public ConductorStatusService(ConductorStatusRepository conductorStatusRepository,
            PassTypeRepository passTypeRepository,
            PassRepository passRepository) {
        this.conductorStatusRepository = conductorStatusRepository;
        this.passTypeRepository = passTypeRepository;
        this.passRepository = passRepository;
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
        try {
            deleteBus(request);
        } catch (BusNotFoundException ignored) {
            // ignore missing bus on reset
        }
        return initializeBusStatus(request.getRouteId(), request.getBusNumber());
    }

    public ResetTripResponse initializeBusStatus(String routeId, String busNumber)
            throws InterruptedException, ExecutionException {
        DocumentSnapshot routeSnapshot = conductorStatusRepository.findRoute(routeId);
        if (!routeSnapshot.exists()) {
            throw new RouteNotFoundException(routeId);
        }

        java.util.List<String> routeStops = conductorStatusRepository.findRouteStops(routeId);
        int stopCount = routeStops == null ? 0 : routeStops.size();

        conductorStatusRepository.resetTrip(routeId, busNumber, stopCount, false);
        return new ResetTripResponse(routeId, busNumber, 0, stopCount, java.time.Instant.now().toString());
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

    public PassTypeListResponse getAllPassTypes() {
        return new PassTypeListResponse(
                passTypeRepository.findAll().stream().map(PassType::getPassType).toList());
    }

    public RouteDetailsResponse getRouteDetails(String routeId)
            throws InterruptedException, ExecutionException {
        DocumentSnapshot snapshot = conductorStatusRepository.findRoute(routeId);
        if (!snapshot.exists()) {
            throw new RouteNotFoundException(routeId);
        }
        return new RouteDetailsResponse(routeId, snapshot.getData() != null ? snapshot.getData() : Map.of());
    }

    public boolean busExists(String routeId, String busNumber)
            throws InterruptedException, ExecutionException {
        return conductorStatusRepository.findBus(routeId, busNumber).exists();
    }

    public Pass validatePass(Long userId, String passType) {
        return passRepository.findFirstByUserIdAndPassType_PassType(userId, passType)
                .filter(pass -> pass.getExpiry() != null && !pass.getExpiry().isBefore(java.time.LocalDateTime.now()))
                .orElseThrow(() -> new IllegalArgumentException("Pass is invalid, expired, or not found."));
    }

    private boolean isAllowedPaymentMethod(String paymentMethod) {
        return paymentMethod != null && ALLOWED_PAYMENT_METHODS.contains(paymentMethod.trim().toLowerCase());
    }

    private boolean isPassPaymentMethod(String paymentMethod) {
        return paymentMethod != null && "passes".equalsIgnoreCase(paymentMethod.trim());
    }

    private boolean isAllowedPassType(String passType) {
        return passType != null && ALLOWED_PASS_TYPES.contains(passType.trim().toLowerCase());
    }

    private boolean passTypeRequiresStops(String passType) {
        return passType != null && PASS_TYPES_WITH_ROUTE_STOPS.contains(passType.trim().toLowerCase());
    }

    public IssueTicketResponse issueTicket(String routeId, String busNumber, String paymentMethod,
            String passType, Long userId, String destination, java.util.List<String> stops)
            throws InterruptedException, ExecutionException {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Payment method is required.");
        }
        if (!isAllowedPaymentMethod(paymentMethod)) {
            throw new IllegalArgumentException("Unsupported payment method.");
        }

        java.util.List<String> routeStops = conductorStatusRepository.findRouteStops(routeId);
        java.util.List<String> stopsList = (routeStops == null || routeStops.isEmpty()) ? stops : routeStops;

        if (stopsList == null || stopsList.isEmpty()) {
            throw new IllegalArgumentException("Stops list is required either from the route or from the request.");
        }

        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Destination is required.");
        }

        DocumentSnapshot snapshot = conductorStatusRepository.findBus(routeId, busNumber);
        if (!snapshot.exists()) {
            throw new BusNotFoundException(routeId, busNumber);
        }

        Long currentStopLong = snapshot.getLong("currentStop");
        Integer currentStopValue = currentStopLong == null ? null : currentStopLong.intValue();
        if (currentStopValue == null) {
            throw new IllegalArgumentException("Current stop not available for this bus.");
        }

        if (isPassPaymentMethod(paymentMethod)) {
            if (passType == null || passType.isBlank() || userId == null) {
                throw new IllegalArgumentException("Pass type and user ID are required for pass-based payments.");
            }
            if (!isAllowedPassType(passType)) {
                throw new IllegalArgumentException("Unsupported pass type for pass-based payment.");
            }
            Pass pass = validatePass(userId, passType);

            String passFromStop = pass.getFromStop();
            String passToStop = pass.getToStop();

            if (passTypeRequiresStops(passType)) {
                if (passFromStop == null || passFromStop.isBlank() || passToStop == null || passToStop.isBlank()) {
                    throw new IllegalArgumentException("This pass type requires both origin and destination stops.");
                }
            }

            if (passFromStop != null && !passFromStop.isBlank()) {
                if (!stopsList.contains(passFromStop)) {
                    throw new IllegalArgumentException("The pass origin stop is not on this bus route.");
                }
            }

            if (passToStop != null && !passToStop.isBlank()) {
                if (!stopsList.contains(passToStop)) {
                    throw new IllegalArgumentException("The pass destination stop is not on this bus route.");
                }
            }

            if (passFromStop != null && !passFromStop.isBlank() && passToStop != null && !passToStop.isBlank()) {
                int fromIndex = stopsList.indexOf(passFromStop);
                int toIndex = stopsList.indexOf(passToStop);
                if (fromIndex >= toIndex) {
                    throw new IllegalArgumentException("The pass origin and destination are not in the correct order for this route.");
                }
            }

            if (passToStop != null && !passToStop.isBlank()) {
                int passToIndex = stopsList.indexOf(passToStop);
                if (passToIndex <= currentStopValue) {
                    throw new IllegalArgumentException("The pass destination is not after the current stop.");
                }
                if (!passToStop.equals(destination)) {
                    throw new IllegalArgumentException("The pass destination does not match the selected destination.");
                }
            }
        } else if (passType != null && !passType.isBlank()) {
            throw new IllegalArgumentException("Pass type can only be used with the passes payment method.");
        }

        if (!stopsList.contains(destination)) {
            throw new IllegalArgumentException("Destination must be one of the route stops.");
        }

        int destinationIndex = stopsList.indexOf(destination);
        if (destinationIndex <= currentStopValue) {
            throw new IllegalArgumentException("Destination must be after the current stop.");
        }

        Object stopsValue = snapshot.get("stops");
        java.util.List<Object> stopCounts = convertStopsToCountList(stopsValue);
        boolean stringValues = isStringStops(stopsValue);

        for (int i = currentStopValue; i <= destinationIndex; i++) {
            Object currentValue = stopCounts.get(i);
            int count = 0;
            if (currentValue instanceof Number number) {
                count = number.intValue();
            } else if (currentValue instanceof String stringValue && !stringValue.isBlank()) {
                count = Integer.parseInt(stringValue);
            }
            int updatedCount = count + 1;
            stopCounts.set(i, stringValues ? String.valueOf(updatedCount) : updatedCount);
        }

        conductorStatusRepository.updateStops(routeId, busNumber, stopCounts);

        return new IssueTicketResponse(routeId, busNumber, destination, "success", "Ticket issued successfully",
                java.time.Instant.now().toString());
    }

    public DocumentNamesResponse listDocumentNames(String collectionId)
            throws InterruptedException, ExecutionException {
        return new DocumentNamesResponse(collectionId, conductorStatusRepository.listDocumentNames(collectionId));
    }

    private boolean isStringStops(Object stopsValue) {
        return switch (stopsValue) {
            case List<?> list -> list.stream().allMatch(item -> item instanceof String);
            case String s -> s != null;
            default -> false;
        };
    }

    private java.util.List<Object> convertStopsToCountList(Object stopsValue) {
        return switch (stopsValue) {
            case java.util.List<?> list -> new java.util.ArrayList<>(list);
            case String stringValue -> {
                java.util.List<Object> stopCounts = new java.util.ArrayList<>();
                java.util.List<String> values = Arrays.stream(stringValue.replaceAll("[\\[\\] ]", "").split(","))
                        .toList();
                stopCounts.addAll(values);
                yield stopCounts;
            }
            default -> throw new IllegalArgumentException("Unknown stop counts format.");
        };
    }

    public static class BusNotFoundException extends RuntimeException {
        public BusNotFoundException(String routeId, String busNumber) {
            super(String.format("Bus document not found for routeId='%s', busNumber='%s'", routeId, busNumber));
        }
    }

    public static class RouteNotFoundException extends RuntimeException {
        public RouteNotFoundException(String routeId) {
            super(String.format("Route document not found for routeId='%s'", routeId));
        }
    }
}
