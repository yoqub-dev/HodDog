package com.example.hoddog.controller;

import com.example.hoddog.dto.ProfitExpenseChartDto;
import com.example.hoddog.dto.PurchasedProductRowView;
import com.example.hoddog.dto.SoldProductRowDto;
import com.example.hoddog.service.AnalyticsService;
import com.example.hoddog.service.SoldProductsReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SoldProductsReportService soldProductsReportService;

    /**
     * mode=week  -> labels: Mon..Sun (last 7 days)
     * mode=month -> labels: Jan..Dec (last 12 months)
     *
     * start/end optional:
     * - week: start/end LocalDate bo'yicha 7 kun interval
     * - month: start/end LocalDate bo'yicha 12 oy interval
     */
    @GetMapping("/profit-expense")
    public ProfitExpenseChartDto profitExpense(
            @RequestParam(defaultValue = "month") String mode,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        // ✅ AnalyticsService ichida oldin revenue/cogs/profit hisoblangan bo'ladi,
        // biz faqat 2 line qaytaramiz: expense(cogs) va profit

        var summary = analyticsService.getSummary(mode, start, end);

        return new ProfitExpenseChartDto(
                summary.getLabels(),
                summary.getExpense(),     // expense = cogs
                summary.getProfit()
        );
    }


    @GetMapping("/sold-products")
    public Page<SoldProductRowDto> soldProducts(
            @RequestParam(defaultValue = "today") String mode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return soldProductsReportService.getSoldProducts(mode, start, end, pageable);
    }


    @GetMapping("/purchased-products")
    public Page<PurchasedProductRowView> purchasedProducts(
            @RequestParam(defaultValue = "today") String mode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return analyticsService.getPurchasedProducts(mode, start, end, pageable);
    }
}
