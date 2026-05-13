package com.meminksr.pricewatchdogapi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String url;
    private String cssSelector;
    private BigDecimal targetPrice;
    private BigDecimal lastPrice;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<PriceHistory> priceHistories;
}