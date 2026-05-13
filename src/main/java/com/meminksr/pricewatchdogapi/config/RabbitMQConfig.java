package com.meminksr.pricewatchdogapi.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "price-check-queue";

    @Bean
    public Queue priceCheckQueue() {
        return new Queue(QUEUE_NAME, true);
    }
}