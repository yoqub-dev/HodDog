package com.example.hoddog.repository;

import com.example.hoddog.dto.ProfitReportDto;
import com.example.hoddog.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("""
    SELECT 
        p.name,
        SUM(oi.quantity),
        SUM(oi.quantity * oi.price),
        SUM(oi.quantity * p.cost)
    FROM OrderItem oi
    JOIN oi.product p
    JOIN oi.order o
    WHERE o.createdAt BETWEEN :start AND :end
    GROUP BY p.name
""")
    List<Object[]> getProfit(LocalDateTime start, LocalDateTime end);

    @Query(value = """
    SELECT 
        DATE(o.created_at) AS day,
        SUM(oi.quantity * oi.price) AS total_sales,
        SUM(oi.quantity * p.cost) AS total_cost
    FROM order_item oi
    JOIN product p ON oi.product_id = p.id
    JOIN orders o ON oi.order_id = o.id
    WHERE o.created_at BETWEEN :start AND :end
    GROUP BY DATE(o.created_at)
    ORDER BY DATE(o.created_at)
    """, nativeQuery = true)
    List<Object[]> getDailyProfitNative(LocalDateTime start, LocalDateTime end);






}
