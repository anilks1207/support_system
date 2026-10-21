package com.example.support.inventory;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockBalanceRepository
        extends JpaRepository<StockBalance, Long> {

    @EntityGraph(attributePaths = {"stockItem", "location"})
    List<StockBalance> findByLocation_CodeOrderByStockItem_ItemCodeAsc(
            String locationCode
    );

    Optional<StockBalance> findByStockItem_IdAndLocation_Id(
            Long stockItemId, Long locationId);
}
