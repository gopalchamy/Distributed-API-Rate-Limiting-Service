package com.ratelimiter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/gateway")
@Tag(name = "Target Microservices (Rate-Limited)", description = "Simulated backend microservice endpoints protected by the Rate Limiter")
@CrossOrigin(origins = "*")
public class GatewayDemoController {

    private final List<Map<String, Object>> mockProducts = Arrays.asList(
            Map.of("id", 101, "name", "MacBook Pro M3 Max", "price", 3499.99, "stock", 14, "category", "Laptops"),
            Map.of("id", 102, "name", "Sony WH-1000XM5 Headphones", "price", 399.99, "stock", 52, "category", "Audio"),
            Map.of("id", 103, "name", "Dell UltraSharp 32\" 4K Monitor", "price", 899.00, "stock", 27, "category", "Monitors"),
            Map.of("id", 104, "name", "Keychron Q1 Pro Wireless Keyboard", "price", 199.50, "stock", 80, "category", "Accessories"),
            Map.of("id", 105, "name", "Logitech MX Master 3S Mouse", "price", 99.99, "stock", 120, "category", "Accessories")
    );

    @GetMapping("/products")
    @Operation(summary = "Get list of available products (Rate-limited)")
    public ResponseEntity<Map<String, Object>> getProducts(
            @Parameter(description = "API Key passed via header or query param")
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Products retrieved successfully from backend service.");
        response.put("total", mockProducts.size());
        response.put("data", mockProducts);
        response.put("backendServerId", "srv-cluster-node-04");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get product details by ID (Rate-limited)")
    public ResponseEntity<?> getProductById(@PathVariable int id) {
        Optional<Map<String, Object>> match = mockProducts.stream()
                .filter(p -> ((Integer) p.get("id")) == id)
                .findFirst();

        if (match.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "product", match.get(),
                    "backendServerId", "srv-cluster-node-04",
                    "timestamp", System.currentTimeMillis()
            ));
        }
        return ResponseEntity.status(404).body(Map.of("status", "ERROR", "message", "Product not found"));
    }

    @PostMapping("/orders")
    @Operation(summary = "Place a new purchase order (Rate-limited)")
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody(required = false) Map<String, Object> orderPayload) {
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("orderId", orderId);
        response.put("message", "Order processed and queued to warehouse.");
        response.put("payloadReceived", orderPayload != null ? orderPayload : Collections.emptyMap());
        response.put("backendServerId", "srv-orders-worker-01");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search backend catalogue (Rate-limited)")
    public ResponseEntity<Map<String, Object>> searchCatalogue(@RequestParam(defaultValue = "") String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> prod : mockProducts) {
            if (prod.get("name").toString().toLowerCase().contains(query.toLowerCase()) ||
                prod.get("category").toString().toLowerCase().contains(query.toLowerCase())) {
                results.add(prod);
            }
        }
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "query", query,
                "matches", results.size(),
                "data", results,
                "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/status")
    @Operation(summary = "Health check of downstream backend service")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ONLINE",
                "service", "Downstream Microservice Cluster",
                "clusterHealth", "OPTIMAL",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
