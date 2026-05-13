package com.meminksr.pricewatchdogapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//
import com.meminksr.pricewatchdogapi.entity.Product;
import com.meminksr.pricewatchdogapi.entity.PriceHistory;
import com.meminksr.pricewatchdogapi.repository.ProductRepository;
import com.meminksr.pricewatchdogapi.repository.PriceHistoryRepository;

@Service
public class ProductService {

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

    // @Transactional: All database operations within this method operate on an all-or-nothing basis.
    @Transactional
    public Product addProductToTrack(String name, String url, String selector, BigDecimal targetPrice) {

        BigDecimal currentPrice = priceScraperService.scrapePrice(url, selector);

        Product product = new Product();
        product.setName(name);
        product.setUrl(url);
        product.setCssSelector(selector);
        product.setTargetPrice(targetPrice);
        product.setLastPrice(currentPrice);

        Product savedProduct = productRepository.save(product);


        PriceHistory history = new PriceHistory();
        history.setProduct(savedProduct); // We specify which product it belongs to
        history.setPrice(currentPrice);
        history.setTimestamp(LocalDateTime.now());
        priceHistoryRepository.save(history);

        return savedProduct;
    }
}