package com.example.hoddog.service;

import com.example.hoddog.dto.AnalyticsSummaryDto;
import com.example.hoddog.repository.OrderItemRepository;
import com.example.hoddog.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
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

    public AnalyticsSummaryDto getSummary(String mode, LocalDate start, LocalDate end) {

        String m = (mode == null) ? "week" : mode.toLowerCase();

        if (m.equals("month")) {
            return buildMonthly(start, end);
        }
        return buildWeekly(start, end);
    }

    // =========================
    // WEEK (last 7 days)
    // =========================
    private AnalyticsSummaryDto buildWeekly(LocalDate start, LocalDate end) {

        LocalDate today = LocalDate.now();
        LocalDate useEnd = (end != null) ? end : today;
        LocalDate useStart = (start != null) ? start : useEnd.minusDays(6);

        LocalDateTime startDt = useStart.atStartOfDay();
        LocalDateTime endDt = useEnd.plusDays(1).atStartOfDay(); // exclusive

        // revenue map
        Map<LocalDate, Double> revenueMap = toDayMap(orderRepo.sumRevenueDaily(startDt, endDt));
        Map<LocalDate, Double> cogsMap = toDayMap(orderItemRepo.sumCogsDaily(startDt, endDt));

        List<String> labels = new ArrayList<>();
        List<Double> revenue = new ArrayList<>();
        List<Double> cogs = new ArrayList<>();
        List<Double> profit = new ArrayList<>();

        LocalDate d = useStart;
        while (!d.isAfter(useEnd)) {
            labels.add(d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)); // Mon..Sun

            double r = revenueMap.getOrDefault(d, 0.0);
            double c = cogsMap.getOrDefault(d, 0.0);

            revenue.add(r);
            cogs.add(c);
            profit.add(r - c);

            d = d.plusDays(1);
        }

        return new AnalyticsSummaryDto(labels, revenue, cogs, profit);
    }

    // =========================
    // MONTH (last 12 months)
    // =========================
    private AnalyticsSummaryDto buildMonthly(LocalDate start, LocalDate end) {

        YearMonth now = YearMonth.now();
        YearMonth endYm = (end != null) ? YearMonth.from(end) : now;
        YearMonth startYm = (start != null) ? YearMonth.from(start) : endYm.minusMonths(11);

        LocalDateTime startDt = startYm.atDay(1).atStartOfDay();
        LocalDateTime endDt = endYm.plusMonths(1).atDay(1).atStartOfDay(); // exclusive

        Map<YearMonth, Double> revenueMap = toMonthMap(orderRepo.sumRevenueMonthly(startDt, endDt));
        Map<YearMonth, Double> cogsMap = toMonthMap(orderItemRepo.sumCogsMonthly(startDt, endDt));

        List<String> labels = new ArrayList<>();
        List<Double> revenue = new ArrayList<>();
        List<Double> cogs = new ArrayList<>();
        List<Double> profit = new ArrayList<>();

        YearMonth ym = startYm;
        while (!ym.isAfter(endYm)) {
            labels.add(ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)); // Jan..Dec

            double r = revenueMap.getOrDefault(ym, 0.0);
            double c = cogsMap.getOrDefault(ym, 0.0);

            revenue.add(r);
            cogs.add(c);
            profit.add(r - c);

            ym = ym.plusMonths(1);
        }

        return new AnalyticsSummaryDto(labels, revenue, cogs, profit);
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
}
