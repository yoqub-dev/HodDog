package com.example.hoddog.repository;

import com.example.hoddog.dto.ProfitReportDto;
import com.example.hoddog.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(value = """
SELECT 
    p.id AS product_id,
    p.name AS product_name,
    DATE_TRUNC(:period, o.created_at) AS bucket_start,
    DATE_TRUNC(:period, o.created_at) +
        CASE 
            WHEN :period = 'day' THEN INTERVAL '1 day'
            WHEN :period = 'week' THEN INTERVAL '1 week'
            WHEN :period = 'month' THEN INTERVAL '1 month'
            WHEN :period = 'year' THEN INTERVAL '1 year'
        END AS bucket_end,
    SUM(oi.quantity) AS qty,
    SUM(oi.line_total) AS revenue
FROM order_item oi
JOIN orders o ON oi.order_id = o.id
JOIN product p ON oi.product_id = p.id
WHERE o.created_at BETWEEN :start AND :end
GROUP BY p.id, p.name, bucket_start, bucket_end
ORDER BY bucket_start DESC, qty DESC
""", nativeQuery = true)
    List<Object[]> aggregateSales(@Param("period") String period,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);


    // ✅ WEEK: kun bo‘yicha COGS (line_cogs)
    @Query(value = """
        SELECT date_trunc('day', o.created_at) AS bucket,
               COALESCE(SUM(oi.line_cogs), 0) AS total
        FROM order_item oi
        JOIN orders o ON o.id = oi.order_id
        WHERE o.created_at >= :start AND o.created_at < :end
        GROUP BY bucket
        ORDER BY bucket
    """, nativeQuery = true)
    List<Object[]> sumCogsDaily(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    // ✅ MONTH: oy bo‘yicha COGS
    @Query(value = """
        SELECT date_trunc('month', o.created_at) AS bucket,
               COALESCE(SUM(oi.line_cogs), 0) AS total
        FROM order_item oi
        JOIN orders o ON o.id = oi.order_id
        WHERE o.created_at >= :start AND o.created_at < :end
        GROUP BY bucket
        ORDER BY bucket
    """, nativeQuery = true)
    List<Object[]> sumCogsMonthly(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);




}
