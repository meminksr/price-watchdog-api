package com.meminksr.pricewatchdogapi.service;

import com.meminksr.pricewatchdogapi.config.RabbitMQConfig;
import com.meminksr.pricewatchdogapi.entity.PriceHistory;
import com.meminksr.pricewatchdogapi.entity.Product;
import com.meminksr.pricewatchdogapi.repository.PriceHistoryRepository;
import com.meminksr.pricewatchdogapi.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PriceCheckWorker {

    private static final Logger log = LoggerFactory.getLogger(PriceCheckWorker.class);

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceScraperService priceScraperService;
    private final EmailService emailService;

    public PriceCheckWorker(ProductRepository productRepository,
                            PriceHistoryRepository priceHistoryRepository,
                            PriceScraperService priceScraperService,
                            EmailService emailService) {
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.priceScraperService = priceScraperService;
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processPriceCheck(Long productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found!"));

            BigDecimal currentPrice = priceScraperService.scrapePrice(product.getUrl(), product.getCssSelector());
            BigDecimal lastPrice = product.getLastPrice();

            if (lastPrice == null || currentPrice.compareTo(lastPrice) != 0) {

                log.info("🚨 Price Change! Product: {} | Old: {} -> New: {}", product.getName(), lastPrice, currentPrice);

                product.setLastPrice(currentPrice);
                productRepository.save(product);

                PriceHistory newHistory = new PriceHistory();
                newHistory.setProduct(product);
                newHistory.setPrice(currentPrice);
                newHistory.setTimestamp(LocalDateTime.now());
                priceHistoryRepository.save(newHistory);

                if (lastPrice != null && product.getTargetPrice() != null && currentPrice.compareTo(product.getTargetPrice()) <= 0) {
                    String userEmail = "Please-enter-your-email-address@gmail.com"; // Please enter your test email address
                    emailService.sendPriceDropEmail(userEmail, product.getName(), product.getTargetPrice(), currentPrice, product.getUrl());
                }

            } else {
                log.info("✅ Check complete, price unchanged: {}", product.getName());
            }

        } catch (Exception e) {
            log.error("❌ An error occurred (Product ID: {}): {}", productId, e.getMessage());
        }
    }
}