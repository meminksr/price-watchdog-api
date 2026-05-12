package com.meminksr.pricewatchdogapi.service;


import com.meminksr.pricewatchdogapi.entity.PriceHistory;
import com.meminksr.pricewatchdogapi.entity.Product;
import com.meminksr.pricewatchdogapi.repository.PriceHistoryRepository;
import com.meminksr.pricewatchdogapi.repository.ProductRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PriceUpdateScheduler {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceScraperService priceScraperService;

    public PriceUpdateScheduler(ProductRepository productRepository,
                                PriceHistoryRepository priceHistoryRepository,
                                PriceScraperService priceScraperService) {
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.priceScraperService = priceScraperService;
    }

    // fixedRate = 30000 demek, bu metodu her 30 saniyede bir (30.000 milisaniye) otomatik çalıştır demektir.
    // Canlıya alırken bunu örneğin her saat başı yapacak şekilde (@Scheduled(cron = "0 0 * * * *")) değiştireceğiz.
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void updateAllPrices() {
        System.out.println("--- Otomatik Fiyat Kontrolü Başladı (" + LocalDateTime.now() + ") ---");

        // 1. Veritabanındaki tüm ürünleri getir
        List<Product> products = productRepository.findAll();

        // 2. Her bir ürün için dön
        for (Product product : products) {
            try {
                // Siteden anlık fiyatı çek
                BigDecimal currentPrice = priceScraperService.scrapePrice(product.getUrl(), product.getCssSelector());
                BigDecimal lastPrice = product.getLastPrice();

                // 3. Eğer fiyat eskisinden farklıysa güncelleme yap
                if (currentPrice.compareTo(lastPrice) != 0) {
                    System.out.println("🚨 Fiyat Değişimi Tespit Edildi! Ürün: " + product.getName() + " | Eski: " + lastPrice + " -> Yeni: " + currentPrice);

                    // Ürünün son fiyatını güncelle
                    product.setLastPrice(currentPrice);
                    productRepository.save(product);

                    // Tarihçeye yeni bir kayıt at
                    PriceHistory newHistory = new PriceHistory();
                    newHistory.setProduct(product);
                    newHistory.setPrice(currentPrice);
                    newHistory.setTimestamp(LocalDateTime.now());
                    priceHistoryRepository.save(newHistory);

                    // Eğer fiyat hedeflenen fiyatın altındaysa (Şimdilik sadece konsola yazıyoruz, bildirim sistemini sonra yapacağız)
                    if (currentPrice.compareTo(product.getTargetPrice()) <= 0) {
                        System.out.println("🎉 HEDEF FİYATA ULAŞILDI! Kullanıcıya e-posta gönderilecek: " + product.getName());
                    }
                } else {
                    System.out.println("✅ " + product.getName() + " için fiyat aynı kaldı: " + currentPrice);
                }

            } catch (Exception e) {
                System.out.println("❌ Ürün kontrol edilirken hata oluştu: " + product.getName() + " - Hata: " + e.getMessage());
            }
        }
        System.out.println("--- Kontrol Tamamlandı ---");
    }
}