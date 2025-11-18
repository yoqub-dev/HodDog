package com.example.hoddog.service;

import com.example.hoddog.dto.DailyProfitDto;
import com.example.hoddog.repository.OrderItemRepository;
import com.example.hoddog.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyProfitService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;

    public List<DailyProfitDto> getDaily(LocalDateTime start, LocalDateTime end) {

        // start null bo‘lsa — eng eski buyurtma sanasidan olib kelamiz
        if (start == null) {
            start = orderRepository.findOldestOrderDate()
                    .orElse(LocalDateTime.now().minusYears(10)); // fallback
        }

        // end null bo‘lsa — hozirgi vaqt
        if (end == null) {
            end = LocalDateTime.now();
        }

        List<Object[]> rows = orderItemRepository.getDailyProfitNative(start, end);

        return rows.stream().map(r -> {
            LocalDate date = ((java.sql.Date) r[0]).toLocalDate();

            Number salesNum = (Number) r[1];
            Number costNum = (Number) r[2];

            double totalSales = salesNum == null ? 0 : salesNum.doubleValue();
            double totalCost = costNum == null ? 0 : costNum.doubleValue();

            double profit = totalSales - totalCost;
            double margin = totalSales > 0 ? (profit / totalSales) * 100 : 0;

            return new DailyProfitDto(
                    date, totalSales, totalCost, profit, margin
            );
        }).toList();
    }

}
