package com.shah_s.bakery_order_service.dto;

import com.shah_s.bakery_order_service.entity.Order;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusUpdateRequest {

    // Getters and Setters
    @NotNull(message = "Status is required")
    private Order.OrderStatus status;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason; // For cancellations

    // Constructors
    public OrderStatusUpdateRequest() {}

    public OrderStatusUpdateRequest(Order.OrderStatus status) {
        this.status = status;
    }

}
