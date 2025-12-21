package com.example.hoddog.service;

import com.example.hoddog.dto.ProfitExpenseChartDto;
import com.example.hoddog.dto.PurchasedProductRowView;
import com.example.hoddog.repository.OrderItemRepository;
import com.example.hoddog.repository.OrderRepository;
import com.example.hoddog.repository.PurchaseOrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;

    public ProfitExpenseChartDto getSummary(String mode, LocalDate start, LocalDate end) {

        String m = (mode == null) ? "week" : mode.toLowerCase();

        if (m.equals("month")) {
            return buildMonthly(start, end);
        }
        return buildWeekly(start, end);
    }

    // =========================
    // WEEK (last 7 days)
    // =========================
    private ProfitExpenseChartDto buildWeekly(LocalDate start, LocalDate end) {

        LocalDate today = LocalDate.now();
        LocalDate useEnd = (end != null) ? end : today;
        LocalDate useStart = (start != null) ? start : useEnd.minusDays(6);

        LocalDateTime startDt = useStart.atStartOfDay();
        LocalDateTime endDt = useEnd.plusDays(1).atStartOfDay(); // exclusive

        // revenue map
        Map<LocalDate, Double> revenueMap = toDayMap(orderRepo.sumRevenueDaily(startDt, endDt));
        Map<LocalDate, Double> cogsMap = toDayMap(orderItemRepo.sumCogsDaily(startDt, endDt));

        List<String> labels = new ArrayList<>();
        List<Double> cogs = new ArrayList<>();
        List<Double> profit = new ArrayList<>();

        LocalDate d = useStart;
        while (!d.isAfter(useEnd)) {
            labels.add(d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));

            double r = revenueMap.getOrDefault(d, 0.0);
            double c = cogsMap.getOrDefault(d, 0.0);

            cogs.add(c);
            profit.add(r - c);

            d = d.plusDays(1);
        }

        return new ProfitExpenseChartDto(labels, cogs, profit);

    }

    // =========================
    // MONTH (last 12 months)
    // =========================
    private ProfitExpenseChartDto buildMonthly(LocalDate start, LocalDate end) {

        YearMonth now = YearMonth.now();
        YearMonth endYm = (end != null) ? YearMonth.from(end) : now;
        YearMonth startYm = (start != null) ? YearMonth.from(start) : endYm.minusMonths(11);

        LocalDateTime startDt = startYm.atDay(1).atStartOfDay();
        LocalDateTime endDt = endYm.plusMonths(1).atDay(1).atStartOfDay(); // exclusive

        Map<YearMonth, Double> revenueMap = toMonthMap(orderRepo.sumRevenueMonthly(startDt, endDt));
        Map<YearMonth, Double> cogsMap = toMonthMap(orderItemRepo.sumCogsMonthly(startDt, endDt));

        List<String> labels = new ArrayList<>();
        List<Double> cogs = new ArrayList<>();
        List<Double> profit = new ArrayList<>();

        YearMonth ym = startYm;
        while (!ym.isAfter(endYm)) {
            labels.add(ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)); // Jan..Dec

            double r = revenueMap.getOrDefault(ym, 0.0);
            double c = cogsMap.getOrDefault(ym, 0.0);

            cogs.add(c);
            profit.add(r - c);

            ym = ym.plusMonths(1);
        }

        return new ProfitExpenseChartDto(labels, cogs, profit);

    }

    // =========================
    // Helpers
    // =========================
    private Map<LocalDate, Double> toDayMap(List<Object[]> rows) {
        Map<LocalDate, Double> map = new HashMap<>();
        for (Object[] r : rows) {
            Timestamp ts = (Timestamp) r[0];
            LocalDate day = ts.toLocalDateTime().toLocalDate();
            double total = r[1] != null ? ((Number) r[1]).doubleValue() : 0.0;
            map.put(day, total);
        }
        return map;
    }

    private Map<YearMonth, Double> toMonthMap(List<Object[]> rows) {
        Map<YearMonth, Double> map = new HashMap<>();
        for (Object[] r : rows) {
            Timestamp ts = (Timestamp) r[0];
            YearMonth ym = YearMonth.from(ts.toLocalDateTime().toLocalDate());
            double total = r[1] != null ? ((Number) r[1]).doubleValue() : 0.0;
            map.put(ym, total);
        }
        return map;
    }

    public Page<PurchasedProductRowView> getPurchasedProducts(String mode,
                                                              LocalDate start,
                                                              LocalDate end,
                                                              Pageable pageable) {

        String m = (mode == null) ? "today" : mode.toLowerCase();
        LocalDate today = LocalDate.now();

        LocalDate startD;
        LocalDate endExclusive;

        switch (m) {
            case "today" -> {
                startD = today;
                endExclusive = today.plusDays(1);
            }
            case "week" -> {
                LocalDate useEnd = (end != null) ? end : today;
                LocalDate useStart = (start != null) ? start : useEnd.minusDays(6);
                startD = useStart;
                endExclusive = useEnd.plusDays(1);
            }
            case "month" -> {
                YearMonth endYm = (end != null) ? YearMonth.from(end) : YearMonth.now();
                YearMonth startYm = (start != null) ? YearMonth.from(start) : endYm;

                startD = startYm.atDay(1);
                endExclusive = endYm.plusMonths(1).atDay(1);
            }
            case "year" -> {
                int y = (end != null) ? end.getYear() : today.getYear();
                startD = LocalDate.of(y, 1, 1);
                endExclusive = LocalDate.of(y + 1, 1, 1);
            }
            case "custom" -> {
                if (start == null || end == null) {
                    throw new RuntimeException("custom mode uchun start va end shart");
                }
                startD = start;
                endExclusive = end.plusDays(1);
            }
            default -> throw new RuntimeException("Noto‘g‘ri mode: " + mode);
        }

        return purchaseOrderItemRepository.topPurchasedProductsPage(startD, endExclusive, pageable);
    }


}
