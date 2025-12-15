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

        // safety: if items null, treat as empty
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            order.setSubtotal(0.0);
            order.setFinalAmount(0.0);
            return orderRepo.save(order);
        }

        // =========================================================
        // 1) ITEMS PROCESSING
        // =========================================================
        for (SaleItemRequest itemReq : dto.getItems()) {

            Product p = productRepo.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            double productPrice = p.getPrice() != null ? p.getPrice() : 0.0;
            double modifiersTotal = 0.0;                  // <-- declare ONCE here
            double itemTotal = productPrice * itemReq.getQuantity();

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .price(productPrice)
                    .quantity(itemReq.getQuantity())
                    .build();

            // =============== MODIFIERS (OPTIONAL) =================
            if (itemReq.getModifierOptionIds() != null && !itemReq.getModifierOptionIds().isEmpty()) {

                for (UUID optId : itemReq.getModifierOptionIds()) {

                    ModifierOption opt = optionRepo.findById(optId).orElse(null);

                    if (opt == null) {
                        // skip missing option (do not throw)
                        continue;
                    }

                    OrderModifier om = OrderModifier.builder()
                            .orderItem(oi)
                            .option(opt)
                            .price(opt.getPrice())
                            .build();

                    oi.getModifiers().add(om);

                    modifiersTotal += opt.getPrice() != null ? opt.getPrice() : 0.0;
                }
            }

            // add modifiers to item total and set lineTotal
            itemTotal += modifiersTotal;
            oi.setLineTotal(itemTotal);

            // add order item
            order.getItems().add(oi);

            // accumulate subtotal
            subtotal += itemTotal;

            // =========================================================
            // 2) TRACK STOCK - minus principal product quantity
            // =========================================================
            if (p.isTrackStock()) {
                Double currentQty = p.getQuantity() != null ? p.getQuantity() : 0.0;
                p.setQuantity(currentQty - itemReq.getQuantity());

                Product savedProduct = productRepo.save(p);
            }

            // =========================================================
            // 3) COMPOSITE INGREDIENT MINUS (AUTOMATIC)
            // =========================================================
            if (p.isComposite() && p.getIngredients() != null) {
                for (CompositeItem ci : p.getIngredients()) {
                    Product ing = ci.getIngredientProduct();
                    if (ing == null) continue;

                    double minusQty = ci.getQuantity() * itemReq.getQuantity();
                    Double ingQty = ing.getQuantity() != null ? ing.getQuantity() : 0.0;
                    ing.setQuantity(ingQty - minusQty);

                    Product savedIng = productRepo.save(ing);
                }
            }

        }

        order.setSubtotal(subtotal);


                // 4) DISCOUNT

        double discountAmount = 0.0;

        UUID disId = dto.getDiscountId();

        if (disId != null) {


            Discount dis = discountRepo.findById(disId).orElse(null);

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
        // 5) FINAL AMOUNT
        // =========================================================
        double finalAmount = subtotal - discountAmount;
        if (finalAmount < 0) finalAmount = 0.0;

        order.setFinalAmount(finalAmount);

        // =========================================================
        // 6) PAYMENT + CHANGE
        // =========================================================
        if (dto.getPaidAmount() != null) {
            order.setPaidAmount(dto.getPaidAmount());
        }

        return orderRepo.save(order);
    }


    public List<ProductSalesReportDto> getSales(String period, LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resolvedEnd = end != null ? end : now;

        // periodga qarab mantiqiy default
        LocalDateTime resolvedStart;
        switch (period.toLowerCase()) {
            case "week" -> resolvedStart = resolvedEnd.minusWeeks(1);
            case "month" -> resolvedStart = resolvedEnd.minusMonths(1);
            case "year" -> resolvedStart = resolvedEnd.minusYears(1);
            default -> resolvedStart = resolvedEnd.minusDays(30); // day yoki notanish qiymat uchun
        }

        LocalDateTime useStart = start != null ? start : resolvedStart;
        LocalDateTime useEnd = resolvedEnd;

        List<Object[]> rows = orderItemRepository.aggregateSales(period, useStart, useEnd);
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
