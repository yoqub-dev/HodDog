package com.example.hoddog.service;

import com.example.hoddog.dto.SoldProductRowDto;
import com.example.hoddog.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
@RequiredArgsConstructor
public class SoldProductsReportService {

    private final OrderItemRepository orderItemRepository;

    public Page<SoldProductRowDto> getSoldProducts(String mode, LocalDate start, LocalDate end, Pageable pageable) {

        String m = (mode == null) ? "today" : mode.toLowerCase();
        LocalDate today = LocalDate.now();

        LocalDateTime startDt;
        LocalDateTime endDt;

        switch (m) {
            case "today" -> {
                startDt = today.atStartOfDay();
                endDt = today.plusDays(1).atStartOfDay();
            }
            case "week" -> {
                LocalDate useEnd = (end != null) ? end : today;
                LocalDate useStart = (start != null) ? start : useEnd.minusDays(6);
                startDt = useStart.atStartOfDay();
                endDt = useEnd.plusDays(1).atStartOfDay();
            }
            case "month" -> {
                YearMonth endYm = (end != null) ? YearMonth.from(end) : YearMonth.now();
                YearMonth startYm = (start != null) ? YearMonth.from(start) : endYm;
                startDt = startYm.atDay(1).atStartOfDay();
                endDt = endYm.plusMonths(1).atDay(1).atStartOfDay();
            }
            case "year" -> {
                int y = (end != null) ? end.getYear() : today.getYear();
                startDt = LocalDate.of(y, 1, 1).atStartOfDay();
                endDt = LocalDate.of(y + 1, 1, 1).atStartOfDay();
            }
            case "custom" -> {
                if (start == null || end == null) {
                    throw new RuntimeException("custom mode uchun start va end shart");
                }
                startDt = start.atStartOfDay();
                endDt = end.plusDays(1).atStartOfDay();
            }
            default -> throw new RuntimeException("Noto‘g‘ri mode: " + mode);
        }

        return orderItemRepository.topSoldProductsPage(startDt, endDt, pageable);
    }
}
