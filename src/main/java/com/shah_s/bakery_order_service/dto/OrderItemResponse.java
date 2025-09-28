package com.shah_s.bakery_order_service.dto;

import com.shah_s.bakery_order_service.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class OrderItemResponse {

    // Getters and Setters
    private UUID id;
    private UUID productId;
    private String productSku;
    private String productName;
    private String productCategory;
    private String productDescription;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPerItem;
    private BigDecimal effectiveUnitPrice;
    private BigDecimal subtotal;
    private String specialInstructions;
    private Integer preparationTimeMinutes;
    private Integer totalPreparationTime;
    private Boolean hasDiscount;
    private LocalDateTime createdAt;

    // Constructors
    public OrderItemResponse() {}

    // Static factory method
    public static OrderItemResponse from(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.id = orderItem.getId();
        response.productId = orderItem.getProductId();
        response.productSku = orderItem.getProductSku();
        response.productName = orderItem.getProductName();
        response.productCategory = orderItem.getProductCategory();
        response.productDescription = orderItem.getProductDescription();
        response.productImageUrl = orderItem.getProductImageUrl();
        response.quantity = orderItem.getQuantity();
        response.unitPrice = orderItem.getUnitPrice();
        response.discountPerItem = orderItem.getDiscountPerItem();
        response.effectiveUnitPrice = orderItem.getEffectiveUnitPrice();
        response.subtotal = orderItem.getSubtotal();
        response.specialInstructions = orderItem.getSpecialInstructions();
        response.preparationTimeMinutes = orderItem.getPreparationTimeMinutes();
        response.totalPreparationTime = orderItem.getTotalPreparationTime();
        response.hasDiscount = orderItem.hasDiscount();
        response.createdAt = orderItem.getCreatedAt();
        return response;
    }

}
