package com.meminksr.pricewatchdogapi.repository;

import com.meminksr.pricewatchdogapi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}