package com.example.hoddog.service;

import com.example.hoddog.dto.*;
import com.example.hoddog.entity.*;
import com.example.hoddog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepo;
    private final SupplierRepository supplierRepo;
    private final ProductRepository productRepo;

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

            // AVERAGE COST FORMULA
            double oldQty = product.getQuantity() != null ? product.getQuantity() : 0;
            double oldCost = product.getCost() != null ? product.getCost() : finalCost;

            double newQty = item.getQuantity();

            double newAverageCost =
                    (oldQty * oldCost + newQty * finalCost)
                            / (oldQty + newQty);

            product.setCost(newAverageCost);
            product.setQuantity(oldQty + newQty);

            productRepo.save(product);
        }
    }
}

