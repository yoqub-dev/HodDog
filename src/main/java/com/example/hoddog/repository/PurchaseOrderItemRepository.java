package com.example.hoddog.repository;

import com.example.hoddog.dto.PurchasedProductRowView;
import com.example.hoddog.entity.PurchaseOrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, UUID> {

    @Query(
            value = """
                SELECT
                    p.sku AS sku,
                    p.name AS productName,
                    COALESCE(SUM(poi.quantity), 0) AS totalOrder,
                    COALESCE(SUM(poi.subtotal), 0) AS totalAmount
                FROM purchase_order_item poi
                JOIN purchase_order po ON po.id = poi.purchase_order_id
                JOIN product p ON p.id = poi.product_id
                WHERE po.purchased_date >= :start AND po.purchased_date < :end
                GROUP BY p.sku, p.name
                ORDER BY totalOrder DESC
            """,
            countQuery = """
                SELECT COUNT(*) FROM (
                    SELECT p.id
                    FROM purchase_order_item poi
                    JOIN purchase_order po ON po.id = poi.purchase_order_id
                    JOIN product p ON p.id = poi.product_id
                    WHERE po.purchased_date >= :start AND po.purchased_date < :end
                    GROUP BY p.id
                ) x
            """,
            nativeQuery = true
    )
    Page<PurchasedProductRowView> topPurchasedProductsPage(@Param("start") LocalDate start,
                                                           @Param("end") LocalDate end,
                                                           Pageable pageable);
}
