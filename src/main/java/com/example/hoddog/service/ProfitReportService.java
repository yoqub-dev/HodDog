package com.example.hoddog.service;

import com.example.hoddog.dto.ProfitReportDto;
import com.example.hoddog.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfitReportService {

    private final OrderItemRepository orderItemRepository;

    public List<ProfitReportDto> getProfit(LocalDateTime start, LocalDateTime end) {

        List<Object[]> rows = orderItemRepository.getProfit(start, end);

        return rows.stream().map(r -> {
            String name = (String) r[0];
            Long qty = (Long) r[1];
            Double totalSales = (Double) r[2];
            Double totalCost = (Double) r[3];

            Double profit = totalSales - totalCost;
            Double margin = totalSales != 0 ? (profit / totalSales) * 100 : 0;

            return new ProfitReportDto(
                    name,
                    qty,
                    totalSales,
                    totalCost,
                    profit,
                    margin
            );
        }).toList();
    }

}
