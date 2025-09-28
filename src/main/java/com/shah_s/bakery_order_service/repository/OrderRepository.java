package com.shah_s.bakery_order_service.repository;

import com.shah_s.bakery_order_service.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Find order by order number
    Optional<Order> findByOrderNumber(String orderNumber);

    // Check if order number exists
    boolean existsByOrderNumber(String orderNumber);

    // Find orders by user ID
    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // Find orders by user ID with pagination
    Page<Order> findByUserId(UUID userId, Pageable pageable);

    // Find orders by status
    List<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status);

    // Find orders by status with pagination
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    // Find orders by multiple statuses
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findByStatusIn(@Param("statuses") List<Order.OrderStatus> statuses);

    // Find orders by delivery type
    List<Order> findByDeliveryTypeOrderByCreatedAtDesc(Order.DeliveryType deliveryType);

    // Find orders by date range
    List<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    // Find orders by date range with pagination
    Page<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find orders by user and status
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, Order.OrderStatus status);

    // Find orders by user and multiple statuses
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndStatusIn(@Param("userId") UUID userId, @Param("statuses") List<Order.OrderStatus> statuses);

    // Find recent orders (last N days)
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :sinceDate ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(@Param("sinceDate") LocalDateTime sinceDate);

    // Find orders pending confirmation (older than X minutes)
    @Query("SELECT o FROM Order o " +
            "WHERE o.status = 'PENDING' " +
            "AND o.createdAt <= :cutoffTime " +
            "ORDER BY o.createdAt ASC")
    List<Order> findPendingOrdersOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find orders ready for delivery/pickup
    @Query("SELECT o FROM Order o " +
            "WHERE o.status = 'READY' " +
            "AND o.estimatedReadyTime <= :currentTime " +
            "ORDER BY o.estimatedReadyTime ASC")
    List<Order> findReadyOrders(@Param("currentTime") LocalDateTime currentTime);

    // Find orders by total amount range
    List<Order> findByTotalAmountBetweenOrderByCreatedAtDesc(BigDecimal minAmount, BigDecimal maxAmount);

    // Search orders by customer name or email
    @Query("SELECT o FROM Order o " +
            "WHERE LOWER(o.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(o.customerEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY o.createdAt DESC")
    List<Order> searchByCustomerInfo(@Param("searchTerm") String searchTerm);

    // ✅ REMOVED: Payment method queries (now handled by Payment Service)
    // These methods are removed since Payment entity is no longer in Order Service:
    // - findByPaymentMethod
    // - findOrdersWithFailedPayments
    // - findOrdersWithPendingPayments

    // Count orders by status
    long countByStatus(Order.OrderStatus status);

    // Count orders by user
    long countByUserId(UUID userId);

    // Count orders by date range
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Count orders by status and date range
    long countByStatusAndCreatedAtBetween(Order.OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);

    // Get daily order statistics
    @Query("SELECT DATE(o.createdAt) as orderDate, " +
            "COUNT(o) as orderCount, " +
            "SUM(o.totalAmount) as totalSales, " +
            "AVG(o.totalAmount) as averageOrderValue " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(o.createdAt) " +
            "ORDER BY DATE(o.createdAt) DESC")
    List<Object[]> getDailyOrderStatistics(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // Get order statistics by status
    @Query("SELECT o.status as status, " +
            "COUNT(o) as orderCount, " +
            "SUM(o.totalAmount) as totalAmount " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY o.status")
    List<Object[]> getOrderStatisticsByStatus(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    // Get top customers by order count
    @Query("SELECT o.userId as userId, " +
            "o.customerName as customerName, " +
            "o.customerEmail as customerEmail, " +
            "COUNT(o) as orderCount, " +
            "SUM(o.totalAmount) as totalSpent " +
            "FROM Order o " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY o.userId, o.customerName, o.customerEmail " +
            "ORDER BY COUNT(o) DESC")
    List<Object[]> getTopCustomers(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    // Get average preparation time by delivery type
    @Query("SELECT o.deliveryType as deliveryType, " +
            "AVG(o.estimatedPreparationMinutes) as avgPreparationTime " +
            "FROM Order o " +
            "WHERE o.estimatedPreparationMinutes IS NOT NULL " +
            "GROUP BY o.deliveryType")
    List<Object[]> getAveragePreparationTimeByDeliveryType();

    // Find orders that need preparation time update
    @Query("SELECT o FROM Order o " +
            "WHERE o.status IN ('CONFIRMED', 'PREPARING') " +
            "AND o.estimatedReadyTime IS NULL " +
            "ORDER BY o.confirmedAt ASC")
    List<Order> findOrdersNeedingPreparationTimeUpdate();

    // Find overdue orders (past estimated ready time)
    @Query("SELECT o FROM Order o " +
            "WHERE o.status IN ('CONFIRMED', 'PREPARING') " +
            "AND o.estimatedReadyTime < :currentTime " +
            "ORDER BY o.estimatedReadyTime ASC")
    List<Order> findOverdueOrders(@Param("currentTime") LocalDateTime currentTime);

    // Find orders by delivery date range
    List<Order> findByDeliveryDateBetweenOrderByDeliveryDateAsc(LocalDateTime startDate, LocalDateTime endDate);

    // Find orders with discount
    @Query("SELECT o FROM Order o " +
            "WHERE o.discountAmount > 0 " +
            "ORDER BY o.createdAt DESC")
    List<Order> findOrdersWithDiscount();

    // Find orders by discount code
    List<Order> findByDiscountCodeOrderByCreatedAtDesc(String discountCode);

    // Get revenue statistics
    @Query("SELECT " +
            "COUNT(o) as totalOrders, " +
            "SUM(o.totalAmount) as totalRevenue, " +
            "AVG(o.totalAmount) as averageOrderValue, " +
            "SUM(o.subtotal) as totalSubtotal, " +
            "SUM(o.taxAmount) as totalTax, " +
            "SUM(o.discountAmount) as totalDiscounts, " +
            "SUM(o.deliveryFee) as totalDeliveryFees " +
            "FROM Order o " +
            "WHERE o.status != 'CANCELLED' " +
            "AND o.createdAt BETWEEN :startDate AND :endDate")
    Object[] getRevenueStatistics(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    // ✅ FIXED: Simplified advanced search without Payment entity
    @Query("SELECT o FROM Order o " +
            "WHERE (:userId IS NULL OR o.userId = :userId) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:deliveryType IS NULL OR o.deliveryType = :deliveryType) " +
            "AND (:minAmount IS NULL OR o.totalAmount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR o.totalAmount <= :maxAmount) " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "ORDER BY o.createdAt DESC")
    List<Order> findOrdersWithFilters(@Param("userId") UUID userId,
                                      @Param("status") Order.OrderStatus status,
                                      @Param("deliveryType") Order.DeliveryType deliveryType,
                                      @Param("paymentMethod") Object paymentMethod, // Ignored parameter (kept for compatibility)
                                      @Param("minAmount") BigDecimal minAmount,
                                      @Param("maxAmount") BigDecimal maxAmount,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
}
