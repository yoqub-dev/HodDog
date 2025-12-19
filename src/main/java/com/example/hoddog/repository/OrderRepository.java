package com.example.hoddog.repository;

import com.example.hoddog.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT MIN(o.createdAt) FROM Order o")
    Optional<LocalDateTime> findOldestOrderDate();

    // ✅ WEEK: kun bo‘yicha revenue (final_amount)
    @Query(value = """
        SELECT date_trunc('day', o.created_at) AS bucket,
               COALESCE(SUM(o.final_amount), 0) AS total
        FROM orders o
        WHERE o.created_at >= :start AND o.created_at < :end
        GROUP BY bucket
        ORDER BY bucket
    """, nativeQuery = true)
    List<Object[]> sumRevenueDaily(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    // ✅ MONTH: oy bo‘yicha revenue
    @Query(value = """
        SELECT date_trunc('month', o.created_at) AS bucket,
               COALESCE(SUM(o.final_amount), 0) AS total
        FROM orders o
        WHERE o.created_at >= :start AND o.created_at < :end
        GROUP BY bucket
        ORDER BY bucket
    """, nativeQuery = true)
    List<Object[]> sumRevenueMonthly(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

}
