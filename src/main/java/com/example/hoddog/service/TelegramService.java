package com.example.hoddog.service;

import com.example.hoddog.entity.Product;
import com.example.hoddog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TelegramService {

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.chat-id}")
    private String chatId;

    private final Set<UUID> notifiedProducts = new HashSet<>();


    private final ProductRepository productRepository;
    private final RestTemplate restTemplate = new RestTemplate();


    @Scheduled(fixedRate = 120000)
    public void checkLowStock() {

        List<Product> products = productRepository.findAll();

        for (Product p : products) {

            if (!p.isTrackStock()) continue;
            if (p.getLowQuantity() == null) continue;
            if (p.getQuantity() == null) continue;

            // ✅ Faqat 1 marta yuborilishi uchun
            if (p.getQuantity() <= p.getLowQuantity() && !p.isLowStockNotified()) {

                String message =
                        "⚠️ KAM QOLGAN MAHSULOT!\n\n" +
                                "Mahsulot: " + p.getName() + "\n" +
                                "Qolgan: " + p.getQuantity() + "\n" +
                                "Minimal: " + p.getLowQuantity();

                sendMessage(message);

                // ✅ Faqat flag o‘zgaryapti, lowQuantity O‘ZGARMAYDI
                p.setLowStockNotified(true);
                productRepository.save(p);
            }

            // ✅ Agar keyin yana zaxira to‘ldirilsa — qayta xabar yuborishga tayyor bo‘ladi
            if (p.getQuantity() > p.getLowQuantity()) {
                p.setLowStockNotified(false);
                productRepository.save(p);
            }
        }
    }



    public void sendMessage(String text) {

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        body.put("parse_mode", "HTML");

        restTemplate.postForObject(url, body, String.class);
    }
}


