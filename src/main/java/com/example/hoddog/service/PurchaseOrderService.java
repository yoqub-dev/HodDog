package com.example.hoddog.service;

import com.example.hoddog.dto.*;
import com.example.hoddog.entity.*;
import com.example.hoddog.enums.SoldBy;
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

        // NPE bo'lmasligi uchun listlarni init qilamiz (entity builder default qilmagan bo'lsa ham)
        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(supplier)
                .purchasedDate(dto.getPurchasedDate())
                .notes(dto.getNotes())
                .items(new ArrayList<>())
                .extraCosts(new ArrayList<>())
                .build();

        double totalAmount = 0.0;

        // ITEMLARNI QO‘SHISH
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("Items list is empty");
        }

        for (PurchaseOrderItemDto itemDto : dto.getItems()) {

            Product product = productRepo.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                throw new RuntimeException("Quantity must be greater than 0");
            }
            if (itemDto.getPurchaseCost() == null || itemDto.getPurchaseCost() < 0) {
                throw new RuntimeException("Purchase cost must be >= 0");
            }

            // ✅ SOLD BY tekshiramiz:
            // EACH   -> purchaseCost = 1 dona narxi
            // WEIGHT -> purchaseCost = jami pul (total paid)
            boolean isWeight = isWeight(product);

            // ✅ subtotal:
            // EACH:   qty * unitPrice
            // WEIGHT: totalPaid (purchaseCostning o'zi)
            double subtotal = isWeight
                    ? itemDto.getPurchaseCost()
                    : itemDto.getQuantity() * itemDto.getPurchaseCost();

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
        if (dto.getExtraCosts() != null && !dto.getExtraCosts().isEmpty()) {
            for (PurchaseOrderExtraCostDto ec : dto.getExtraCosts()) {

                if (ec.getAmount() == null || ec.getAmount() < 0) {
                    throw new RuntimeException("Extra cost amount must be >= 0");
                }

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

        // INVENTORY UPDATE + AVERAGE COST
        applyInventoryUpdates(po);

        return poRepo.save(po);
    }

    // INVENTORY UPDATE + AVERAGE COST
    private void applyInventoryUpdates(PurchaseOrder po) {

        double itemsSubtotal = po.getItems().stream()
                .mapToDouble(PurchaseOrderItem::getSubtotal)
                .sum();

        double extraCostTotal = (po.getExtraCosts() == null) ? 0.0 :
                po.getExtraCosts().stream()
                        .mapToDouble(PurchaseOrderExtraCost::getAmount)
                        .sum();

        for (PurchaseOrderItem item : po.getItems()) {

            Product product = item.getProduct();

            // extra cost'larni subtotalga qarab taqsimlaymiz
            double proportion = itemsSubtotal > 0 ? (item.getSubtotal() / itemsSubtotal) : 0.0;
            double allocatedExtra = proportion * extraCostTotal;

            // ✅ finalUnitCost:
            // EACH: purchaseCost = 1 dona narxi
            //       finalUnitCost = unitPrice + allocatedExtra/qty
            //
            // WEIGHT: purchaseCost = jami pul
            //         finalUnitCost = (totalPaid + allocatedExtra)/qty  => 1g narxi
            boolean isWeight = isWeight(product);

            double finalUnitCost;
            if (isWeight) {
                finalUnitCost = (item.getPurchaseCost() + allocatedExtra) / item.getQuantity();
            } else {
                finalUnitCost = item.getPurchaseCost() + (allocatedExtra / item.getQuantity());
            }

            double oldQty = product.getQuantity() != null ? product.getQuantity() : 0.0;
            double oldCost = product.getCost() != null ? product.getCost() : finalUnitCost;

            double newQty = item.getQuantity();
            double updatedQty = oldQty + newQty;

            // average cost: (oldQty*oldCost + newQty*finalUnitCost) / (oldQty+newQty)
            double newAverageCost;
            if (updatedQty > 0) {
                newAverageCost = (oldQty * oldCost + newQty * finalUnitCost) / updatedQty;
            } else {
                newAverageCost = finalUnitCost;
            }

            product.setCost(newAverageCost);
            product.setQuantity(updatedQty);

            // Low stock notified reset (agar limitdan chiqsa)
            if (product.getLowQuantity() != null && updatedQty > product.getLowQuantity()) {
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
            List<Product> parents = productRepo.findAllByIngredientsIngredientProductId(ingredient.getId());
            for (Product parent : parents) {
                affectedIds.add(parent.getId());
            }
        }

        if (!affectedIds.isEmpty()) {
            productService.recalcCompositeCostsForProducts(new ArrayList<>(affectedIds));
        }
    }

    // ✅ Sizning Product entity'ingiz bo'yicha SOLD BY tekshiruvi
    private boolean isWeight(Product product) {
        return product.getSoldBy() != null && product.getSoldBy() == SoldBy.WEIGHT;
    }
}
