package com.example.hoddog.dto;

import com.example.hoddog.enums.PaymentType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SaleRequest {

    private List<SaleItemRequest> items;

    private UUID discountId;
    private PaymentType paymentType;
    private Double paidAmount;
}
