package com.example.hoddog.entity;


import com.example.hoddog.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;


@Entity
@Data
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    private Double subtotal;          // discountdan oldingi summa
    private Double discountAmount;    // chegirma summasi
    private UUID discountId;          // qaysi discount ishlatilgan
    private Double finalAmount;       // yakuniy summa
    private Double paidAmount;        // kassaga tushgan pul

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<OrderItem> items = new ArrayList<>();
}
