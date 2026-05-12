package com.meminksr.pricewatchdogapi.repository;

import com.meminksr.pricewatchdogapi.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
}