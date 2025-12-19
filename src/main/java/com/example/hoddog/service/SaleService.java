package com.example.hoddog.service;

import com.example.hoddog.dto.ProductSalesReportDto;
import com.example.hoddog.dto.SaleItemRequest;
import com.example.hoddog.dto.SaleRequest;
import com.example.hoddog.entity.*;
import com.example.hoddog.enums.DiscountType;
import com.example.hoddog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final ProductRepository productRepo;
    private final DiscountRepository discountRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepository;
    private final ModifierOptionRepository optionRepo;

    public Order createSale(SaleRequest dto) {

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setPaymentType(dto.getPaymentType());

        double subtotal = 0.0;

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            order.setSubtotal(0.0);
            order.setFinalAmount(0.0);
            return orderRepo.save(order);
        }

        // =========================================================
        // 1) ITEMS
        // =========================================================
        for (SaleItemRequest itemReq : dto.getItems()) {

            Product p = productRepo.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            double price = p.getPrice() != null ? p.getPrice() : 0.0;
            double modifiersTotal = 0.0;
            double lineTotal = price * itemReq.getQuantity();

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .price(price)
                    .quantity(itemReq.getQuantity())
                    .build();

            // ================= MODIFIERS =================
            if (itemReq.getModifierOptionIds() != null) {
                for (UUID optId : itemReq.getModifierOptionIds()) {
                    ModifierOption opt = optionRepo.findById(optId).orElse(null);
                    if (opt == null) continue;

                    OrderModifier om = OrderModifier.builder()
                            .orderItem(oi)
                            .option(opt)
                            .price(opt.getPrice())
                            .build();

                    oi.getModifiers().add(om);
                    modifiersTotal += opt.getPrice() != null ? opt.getPrice() : 0.0;
                }
            }

            lineTotal += modifiersTotal;
            oi.setLineTotal(lineTotal);
            subtotal += lineTotal;

            // =========================================================
            // 🔴 2) TANNARX (COGS) HISOBLASH
            // =========================================================
            if (!p.isComposite()) {
                // ODDIY MAHSULOT
                double unitCost = p.getCost() != null ? p.getCost() : 0.0;
                double lineCogs = unitCost * itemReq.getQuantity();

                oi.setUnitCost(unitCost);
                oi.setLineCogs(lineCogs);
            } else {
                // COMPOSITE (HOT-DOG)
                double cogs = 0.0;

                for (CompositeItem ci : p.getIngredients()) {
                    Product ing = ci.getIngredientProduct();
                    if (ing == null) continue;

                    double ingUnitCost = ing.getCost() != null ? ing.getCost() : 0.0;
                    double usedQty = ci.getQuantity() * itemReq.getQuantity();

                    cogs += ingUnitCost * usedQty;
                }

                oi.setUnitCost(null);
                oi.setLineCogs(cogs);
            }

            order.getItems().add(oi);

            // =========================================================
            // 3) STOCK MINUS
            // =========================================================
            if (p.isTrackStock()) {
                double current = p.getQuantity() != null ? p.getQuantity() : 0.0;
                p.setQuantity(current - itemReq.getQuantity());
                productRepo.save(p);
            }

            if (p.isComposite()) {
                for (CompositeItem ci : p.getIngredients()) {
                    Product ing = ci.getIngredientProduct();
                    if (ing == null) continue;

                    double minusQty = ci.getQuantity() * itemReq.getQuantity();
                    double ingQty = ing.getQuantity() != null ? ing.getQuantity() : 0.0;
                    ing.setQuantity(ingQty - minusQty);

                    productRepo.save(ing);
                }
            }
        }

        order.setSubtotal(subtotal);

        // =========================================================
        // 4) DISCOUNT
        // =========================================================
        double discountAmount = 0.0;

        if (dto.getDiscountId() != null) {
            Discount dis = discountRepo.findById(dto.getDiscountId()).orElse(null);
            if (dis != null) {
                order.setDiscountId(dis.getId());
                if (dis.getType() == DiscountType.PERCENTAGE) {
                    discountAmount = subtotal * (dis.getValue() / 100.0);
                } else {
                    discountAmount = dis.getValue();
                }
            }
        }

        order.setDiscountAmount(discountAmount);

        // =========================================================
        // 5) FINAL
        // =========================================================
        double finalAmount = subtotal - discountAmount;
        if (finalAmount < 0) finalAmount = 0.0;
        order.setFinalAmount(finalAmount);

        if (dto.getPaidAmount() != null) {
            order.setPaidAmount(dto.getPaidAmount());
        }

        return orderRepo.save(order);
    }

    // =========================================================
    // SALES REPORT
    // =========================================================
    public List<ProductSalesReportDto> getSales(String period, LocalDateTime start, LocalDateTime end) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resolvedEnd = end != null ? end : now;
        LocalDateTime resolvedStart;

        switch (period.toLowerCase()) {
            case "week" -> resolvedStart = resolvedEnd.minusWeeks(1);
            case "month" -> resolvedStart = resolvedEnd.minusMonths(1);
            case "year" -> resolvedStart = resolvedEnd.minusYears(1);
            default -> resolvedStart = resolvedEnd.minusDays(30);
        }

        List<Object[]> rows = orderItemRepository.aggregateSales(
                period,
                start != null ? start : resolvedStart,
                resolvedEnd
        );

        return rows.stream().map(r -> new ProductSalesReportDto(
                (UUID) r[0],
                (String) r[1],
                ((java.sql.Timestamp) r[2]).toLocalDateTime(),
                ((java.sql.Timestamp) r[3]).toLocalDateTime(),
                ((Number) r[4]).longValue(),
                r[5] != null ? ((Number) r[5]).doubleValue() : 0.0
        )).toList();
    }
}
