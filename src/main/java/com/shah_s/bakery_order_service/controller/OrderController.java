package com.shah_s.bakery_order_service.controller;

import com.shah_s.bakery_order_service.dto.*;
import com.shah_s.bakery_order_service.entity.Order;
import com.shah_s.bakery_order_service.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Create new order
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Create order request received for user: {}", request.getUserId());

        // Use header userId if available (from Gateway), otherwise use request userId
        if (userId != null) {
            request.setUserId(userId);
        }

        OrderResponse order = orderService.createOrder(request);

        logger.info("Order created successfully: {}", order.getOrderNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // Get all orders with pagination
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get all orders request received (page: {}, size: {})", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponse> orders = orderService.getAllOrders(pageable);

        logger.info("Retrieved {} orders (page {} of {})", orders.getContent().size(),
                page + 1, orders.getTotalPages());
        return ResponseEntity.ok(orders);
    }

    // Get order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get order by ID request received: {}", orderId);

        OrderResponse order = orderService.getOrderById(orderId);

        // Check if user can access this order (unless admin)
        if (userId != null && !"ADMIN".equals(userRole) && !order.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        logger.info("Order retrieved: {}", order.getOrderNumber());
        return ResponseEntity.ok(order);
    }

    // Get order by order number
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(
            @PathVariable String orderNumber,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get order by number request received: {}", orderNumber);

        return orderService.getOrderByOrderNumber(orderNumber)
                .map(order -> {
                    // Check if user can access this order (unless admin)
                    if (userId != null && !"ADMIN".equals(userRole) && !order.getUserId().equals(userId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<OrderResponse>build();
                    }
                    logger.info("Order found: {}", orderNumber);
                    return ResponseEntity.ok(order);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Get orders by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get orders by user ID request received: {}", userId);

        // Check if user can access these orders (unless admin)
        if (requestUserId != null && !"ADMIN".equals(userRole) && !userId.equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);

        logger.info("Retrieved {} orders for user", orders.size());
        return ResponseEntity.ok(orders);
    }

    // Get orders by user ID with pagination
    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<Page<OrderResponse>> getOrdersByUserIdWithPagination(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get orders by user ID with pagination: {}, page: {}, size: {}", userId, page, size);

        // Check if user can access these orders (unless admin)
        if (requestUserId != null && !"ADMIN".equals(userRole) && !userId.equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponse> orders = orderService.getOrdersByUserIdWithPagination(userId, pageable);

        logger.info("Retrieved {} orders for user (page {} of {})", orders.getContent().size(),
                page + 1, orders.getTotalPages());
        return ResponseEntity.ok(orders);
    }

    // Get orders by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @PathVariable Order.OrderStatus status) {

        logger.info("Get orders by status request received: {}", status);

        List<OrderResponse> orders = orderService.getOrdersByStatus(status);

        logger.info("Retrieved {} orders with status {}", orders.size(), status);
        return ResponseEntity.ok(orders);
    }

    // Search orders
    @GetMapping("/search")
    public ResponseEntity<List<OrderResponse>> searchOrders(@RequestParam String query) {
        logger.info("Search orders request received with query: {}", query);

        List<OrderResponse> orders = orderService.searchOrders(query);

        logger.info("Search returned {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    // Get recent orders
    @GetMapping("/recent")
    public ResponseEntity<List<OrderResponse>> getRecentOrders(
            @RequestParam(defaultValue = "7") int days) {
        logger.info("Get recent orders request received (last {} days)", days);

        List<OrderResponse> orders = orderService.getRecentOrders(days);

        logger.info("Retrieved {} recent orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    // ✅ FIXED: Advanced search with filters (removed Payment.PaymentMethod)
    @GetMapping("/filter")
    public ResponseEntity<List<OrderResponse>> getOrdersWithFilters(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) Order.DeliveryType deliveryType,
            @RequestParam(required = false) String paymentMethod, // ✅ Changed to String
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("Advanced filter search request received");

        List<OrderResponse> orders = orderService.getOrdersWithFilters(
                userId, status, deliveryType, paymentMethod, minAmount, maxAmount, startDate, endDate);

        logger.info("Filter search returned {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    // Update order status
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Update order status request received: {} to {}", orderId, request.getStatus());

        // Only admins and bakers can update order status
        if (!"ADMIN".equals(userRole) && !"BAKER".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        OrderResponse order = orderService.updateOrderStatus(orderId, request);

        logger.info("Order status updated successfully: {}", orderId);
        return ResponseEntity.ok(order);
    }

    // Cancel order
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable UUID orderId,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Cancel order request received: {}", orderId);

        // Check if user can cancel this order
        if (userId != null && !"ADMIN".equals(userRole)) {
            OrderResponse existingOrder = orderService.getOrderById(orderId);
            if (!existingOrder.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        String reason = request.get("reason");
        OrderResponse order = orderService.cancelOrder(orderId, reason);

        logger.info("Order cancelled successfully: {}", orderId);
        return ResponseEntity.ok(order);
    }

    // Get order statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOrderStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get order statistics request received");

        // Only admins can view statistics
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Default to last 30 days if no dates provided
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, Object> statistics = orderService.getOrderStatistics(startDate, endDate);

        logger.info("Order statistics retrieved");
        return ResponseEntity.ok(statistics);
    }

    // Health check
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "order-service-orders");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    // Payment status update webhook (called by Payment Service)
    @PostMapping("/{orderId}/payment-update")
    public ResponseEntity<Map<String, String>> updateOrderPaymentStatus(
            @PathVariable UUID orderId,
            @RequestBody Map<String, Object> paymentUpdate) {

        logger.info("Payment status update received for order: {} - Status: {}",
                orderId, paymentUpdate.get("status"));

        try {
            // Update order based on payment status
            String paymentStatus = (String) paymentUpdate.get("status");
            OrderStatusUpdateRequest statusUpdate = new OrderStatusUpdateRequest();

            switch (paymentStatus) {
                case "COMPLETED" -> {
                    statusUpdate.setStatus(Order.OrderStatus.CONFIRMED);
                    statusUpdate.setNotes("Payment completed successfully");
                }
                case "FAILED" -> {
                    statusUpdate.setStatus(Order.OrderStatus.CANCELLED);
                    statusUpdate.setReason("Payment failed: " + paymentUpdate.get("gatewayResponse"));
                }
                case "CANCELLED" -> {
                    statusUpdate.setStatus(Order.OrderStatus.CANCELLED);
                    statusUpdate.setReason("Payment cancelled");
                }
                default -> {
                    // Payment still processing, no status change needed
                    logger.info("Payment status {} for order {} - no order status change needed",
                            paymentStatus, orderId);
                }
            }

            if (statusUpdate.getStatus() != null) {
                orderService.updateOrderStatus(orderId, statusUpdate);
            }

            return ResponseEntity.ok(Map.of("status", "updated"));

        } catch (Exception e) {
            logger.error("Failed to update order payment status: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("status", "acknowledged")); // Don't fail Payment Service callback
        }
    }
}
