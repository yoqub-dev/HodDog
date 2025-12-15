package com.example.hoddog.service;

import com.example.hoddog.dto.*;
import com.example.hoddog.entity.*;
import com.example.hoddog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepo;
    private final SupplierRepository supplierRepo;
    private final ProductRepository productRepo;
    private final ProductService productService;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public PurchaseOrder purchase(PurchaseOrderCreateDto dto) {

        Supplier supplier = supplierRepo.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(supplier)
                .purchasedDate(dto.getPurchasedDate())
                .notes(dto.getNotes())
                .build();

        double totalAmount = 0.0;

        // ITEMLARNI QO‘SHISH
        for (PurchaseOrderItemDto itemDto : dto.getItems()) {

            Product product = productRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            double subtotal = itemDto.getQuantity() * itemDto.getPurchaseCost();

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(po)
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .purchaseCost(itemDto.getPurchaseCost())
                    .subtotal(subtotal)
                    .build();

            po.getItems().add(item);
            totalAmount += subtotal;
        }

        // EXTRA COSTLARNI QO‘SHISH
        if (dto.getExtraCosts() != null) {
            for (PurchaseOrderExtraCostDto ec : dto.getExtraCosts()) {

                PurchaseOrderExtraCost extra = PurchaseOrderExtraCost.builder()
                        .purchaseOrder(po)
                        .name(ec.getName())
                        .amount(ec.getAmount())
                        .build();

                po.getExtraCosts().add(extra);
                totalAmount += ec.getAmount();
            }
        }

        po.setTotalAmount(totalAmount);

        // ENDILIKDA INVENTORYNI YANGILAYMIZ
        applyInventoryUpdates(po);

        return poRepo.save(po);
    }


    // INVENTORY UPDATE + AVERAGE COST
    private void applyInventoryUpdates(PurchaseOrder po) {


        double itemsSubtotal = po.getItems().stream()
                .mapToDouble(PurchaseOrderItem::getSubtotal)
                .sum();

        double extraCostTotal = po.getExtraCosts().stream()
                .mapToDouble(PurchaseOrderExtraCost::getAmount)
                .sum();

        for (PurchaseOrderItem item : po.getItems()) {

            Product product = item.getProduct();

            double proportion = item.getSubtotal() / itemsSubtotal;
            double allocatedExtra = proportion * extraCostTotal;

            double finalCost = item.getPurchaseCost() + (allocatedExtra / item.getQuantity());

            double oldQty = product.getQuantity() != null ? product.getQuantity() : 0;
            double oldCost = product.getCost() != null ? product.getCost() : finalCost;

            double newQty = item.getQuantity();

            double newAverageCost =
                    (oldQty * oldCost + newQty * finalCost)
                            / (oldQty + newQty);

            double updatedQty = oldQty + newQty;

            product.setCost(newAverageCost);
            product.setQuantity(updatedQty);

            // ✅ TO‘G‘RI LOGIKA
            if (product.getLowQuantity() != null && updatedQty > product.getLowQuantity()) {
                // 🔄 Endi normal holat — qayta ogohlantirishga ruxsat
                product.setLowStockNotified(false);
            }
            // agar updatedQty <= lowQuantity bo‘lsa → TRUE bo‘lib qoladi
            productRepo.save(product);
        }
        recalcCompositeParents(po);
    }



    public List<PurchaseReportDto> getPurchases(String period, LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();
        LocalDate resolvedEnd = end != null ? end : today;
        LocalDate resolvedStart;
        switch (period.toLowerCase()) {
            case "week" -> resolvedStart = resolvedEnd.minusWeeks(1);
            case "month" -> resolvedStart = resolvedEnd.minusMonths(1);
            case "year" -> resolvedStart = resolvedEnd.minusYears(1);
            default -> resolvedStart = resolvedEnd.minusDays(30);
        }
        LocalDate useStart = start != null ? start : resolvedStart;
        LocalDate useEnd = resolvedEnd;

        return purchaseOrderRepository.aggregatePurchases(period, useStart, useEnd)
                .stream()
                .map(r -> new PurchaseReportDto(
                        (UUID) r[0],
                        (String) r[1],
                        ((java.sql.Timestamp) r[2]).toLocalDateTime(),
                        ((java.sql.Timestamp) r[3]).toLocalDateTime(),
                        ((Number) r[4]).longValue(),
                        r[5] != null ? ((Number) r[5]).doubleValue() : 0.0
                ))
                .toList();
    }

    private void recalcCompositeParents(PurchaseOrder po) {
        // xarid qilingan ingredientlarga bog‘langan composite mahsulotlarni topish
        Set<UUID> affectedIds = new HashSet<>();
        for (PurchaseOrderItem item : po.getItems()) {
            Product ingredient = item.getProduct();
            // Ingredientdan parent composite’larni topish
            List<Product> parents = productRepo.findAllByIngredientsIngredientProductId(ingredient.getId());
            for (Product parent : parents) {
                affectedIds.add(parent.getId());
            }
        }
        if (!affectedIds.isEmpty()) {
            productService.recalcCompositeCostsForProducts(new ArrayList<>(affectedIds));
        }
    }

}

