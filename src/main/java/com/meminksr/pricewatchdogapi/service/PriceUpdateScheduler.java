package com.meminksr.pricewatchdogapi.service;

import com.meminksr.pricewatchdogapi.config.RabbitMQConfig;
import com.meminksr.pricewatchdogapi.entity.Product;
import com.meminksr.pricewatchdogapi.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceUpdateScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceUpdateScheduler.class);

    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;

    public PriceUpdateScheduler(ProductRepository productRepository, RabbitTemplate rabbitTemplate) {
        this.productRepository = productRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    // It will run every 30 seconds and add tasks to the queue
    @Scheduled(fixedRate = 30000)
    public void queueProductsForUpdate() {

        log.info("--- Scheduler Ran: Tasks Are Being Added to the Queue ---");

        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, product.getId());
            log.info("📦 A message has been added to the queue -> Product ID: {}", product.getId());
        }
    }
}