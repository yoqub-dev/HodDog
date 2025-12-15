package com.example.hoddog.repository;

import com.example.hoddog.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    @Query(value = """
SELECT 
    p.id AS product_id,
    p.name AS product_name,
    DATE_TRUNC(:period, po.purchased_date::timestamp) AS bucket_start,
    DATE_TRUNC(:period, po.purchased_date::timestamp) +
        CASE 
            WHEN :period = 'day' THEN INTERVAL '1 day'
            WHEN :period = 'week' THEN INTERVAL '1 week'
            WHEN :period = 'month' THEN INTERVAL '1 month'
            WHEN :period = 'year' THEN INTERVAL '1 year'
        END AS bucket_end,
    SUM(poi.quantity) AS qty,
    SUM(poi.subtotal) AS total_cost
FROM purchase_order_item poi
JOIN purchase_order po ON poi.purchase_order_id = po.id
JOIN product p ON poi.product_id = p.id
WHERE po.purchased_date BETWEEN :start AND :end
GROUP BY p.id, p.name, bucket_start, bucket_end
ORDER BY bucket_start DESC, qty DESC
""", nativeQuery = true)
    List<Object[]> aggregatePurchases(@Param("period") String period,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);
}
