package com.example.hoddog.dto;

public interface PurchasedProductRowView {
    String getSku();
    String getProductName();
    Double getTotalOrder();   // quantity Double bo‘lgani uchun
    Double getTotalAmount();  // subtotal sum
}
