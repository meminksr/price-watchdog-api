package com.meminksr.pricewatchdogapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Senin projendeki doğru dosya yolları (Importlar)
import com.meminksr.pricewatchdogapi.entity.Product;
import com.meminksr.pricewatchdogapi.entity.PriceHistory;
import com.meminksr.pricewatchdogapi.repository.ProductRepository;
import com.meminksr.pricewatchdogapi.repository.PriceHistoryRepository;

@Service
public class ProductService {

    // Bağımlılıkları (Dependencies) içeri alıyoruz
    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceScraperService priceScraperService;

    public ProductService(ProductRepository productRepository,
                          PriceHistoryRepository priceHistoryRepository,
                          PriceScraperService priceScraperService) {
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.priceScraperService = priceScraperService;
    }

    // @Transactional: Bu metodun içindeki tüm veritabanı işlemleri ya hep ya hiç mantığıyla çalışır.
    @Transactional
    public Product addProductToTrack(String name, String url, String selector, BigDecimal targetPrice) {

        // 1. Dış dünyadan o anki güncel fiyatı çek
        BigDecimal currentPrice = priceScraperService.scrapePrice(url, selector);

        // 2. Yeni Ürün nesnesini oluştur ve içini doldur
        Product product = new Product();
        product.setName(name);
        product.setUrl(url);
        product.setCssSelector(selector);
        product.setTargetPrice(targetPrice);
        product.setLastPrice(currentPrice);

        // 3. Ürünü veritabanına kaydet (Kaydettikten sonra ID'si oluşacak)
        Product savedProduct = productRepository.save(product);

        // 4. Bu ürün için ilk "Fiyat Geçmişi" kaydını oluştur
        PriceHistory history = new PriceHistory();
        history.setProduct(savedProduct); // Hangi ürüne ait olduğunu belirtiyoruz
        history.setPrice(currentPrice);
        history.setTimestamp(LocalDateTime.now()); // Şu anki tarih ve saat

        // 5. Geçmişi kaydet
        priceHistoryRepository.save(history);

        return savedProduct; // Kaydedilen ürünü geri döndür
    }
}