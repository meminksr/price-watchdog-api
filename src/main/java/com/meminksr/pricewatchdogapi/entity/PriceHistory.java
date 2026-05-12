package com.meminksr.pricewatchdogapi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal price;
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}