package com.meminksr.pricewatchdogapi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data // Lombok ile getter/setter otomatik oluşur
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String url;
    private String cssSelector;
    private BigDecimal targetPrice; // Kullanıcı bu fiyatın altına düşerse haber ver diyecek
    private BigDecimal lastPrice;   // En son çekilen fiyat

    // Bir ürünün birden fazla fiyat geçmişi olabilir
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<PriceHistory> priceHistories;
}