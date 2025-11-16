package com.example.hoddog.service;

import com.example.hoddog.dto.SaleItemRequest;
import com.example.hoddog.dto.SaleRequest;
import com.example.hoddog.entity.*;
import com.example.hoddog.enums.DiscountType;
import com.example.hoddog.repository.DiscountRepository;
import com.example.hoddog.repository.ModifierOptionRepository;
import com.example.hoddog.repository.OrderRepository;
import com.example.hoddog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final ProductRepository productRepo;
    private final DiscountRepository discountRepo;
    private final OrderRepository orderRepo;
    private final ModifierOptionRepository optionRepo;

    public Order createSale(SaleRequest dto) {

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setPaymentType(dto.getPaymentType());

        double subtotal = 0.0;

        // ======================================
        // 1) ITEMS PROCESSING
        // ======================================
        for (SaleItemRequest itemReq : dto.getItems()) {

            Product p = productRepo.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            double productPrice = p.getPrice();
            double itemTotal = productPrice * itemReq.getQuantity();

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .price(productPrice)
                    .quantity(itemReq.getQuantity())
                    .build();

            // ============ MODIFIERS ================
            double modifiersTotal = 0.0;

            if (itemReq.getModifierOptionIds() != null) {
                for (UUID optId : itemReq.getModifierOptionIds()) {
                    ModifierOption opt = optionRepo.findById(optId)
                            .orElseThrow(() -> new RuntimeException("Option not found"));

                    OrderModifier om = OrderModifier.builder()
                            .orderItem(oi)
                            .option(opt)
                            .price(opt.getPrice())
                            .build();

                    oi.getModifiers().add(om);

                    // modifier narxi
                    modifiersTotal += opt.getPrice();
                }
            }

            itemTotal += modifiersTotal;
            oi.setLineTotal(itemTotal);

            order.getItems().add(oi);

            subtotal += itemTotal;

            // ======================================
            // 2) TRACK STOCK MINUS
            // ======================================
            if (p.isTrackStock()) {
                p.setQuantity(p.getQuantity() - itemReq.getQuantity());
                productRepo.save(p);
            }

            // ======================================
            // 3) COMPOSITE INGREDIENT MINUS
            // ======================================
            if (p.isComposite()) {
                for (CompositeItem ci : p.getIngredients()) {
                    Product ing = ci.getIngredientProduct();
                    int minusQty = ci.getQuantity() * itemReq.getQuantity();

                    ing.setQuantity(ing.getQuantity() - minusQty);
                    productRepo.save(ing);
                }
            }
        }

        order.setSubtotal(subtotal);

        // ======================================
        // 4) DISCOUNT
        // ======================================
        double discountAmount = 0.0;

        if (dto.getDiscountId() != null) {
            Discount dis = discountRepo.findById(dto.getDiscountId())
                    .orElseThrow(() -> new RuntimeException("Discount not found"));

            order.setDiscountId(dis.getId());

            if (dis.getType() == DiscountType.PERCENTAGE) {
                discountAmount = subtotal * (dis.getValue() / 100);
            } else {
                discountAmount = dis.getValue();
            }
        }

        order.setDiscountAmount(discountAmount);

        // ======================================
        // 5) FINAL AMOUNT
        // ======================================
        double finalAmount = subtotal - discountAmount;
        if (finalAmount < 0) finalAmount = 0.0;

        order.setFinalAmount(finalAmount);

        // ======================================
        // 6) PAYMENT + CHANGE
        // ======================================
        if (dto.getPaidAmount() != null) {
            order.setPaidAmount(dto.getPaidAmount());
            order.setChangeAmount(dto.getPaidAmount() - finalAmount);
        }

        return orderRepo.save(order);
    }
}

