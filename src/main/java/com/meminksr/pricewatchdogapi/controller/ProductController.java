package com.meminksr.pricewatchdogapi.controller;

import com.meminksr.pricewatchdogapi.service.ProductService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import com.meminksr.pricewatchdogapi.entity.Product;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/add")
    public Product addProduct(@RequestParam String name,
                              @RequestParam String url,
                              @RequestParam String selector,
                              @RequestParam BigDecimal targetPrice) {

        return productService.addProductToTrack(name, url, selector, targetPrice);
    }
}