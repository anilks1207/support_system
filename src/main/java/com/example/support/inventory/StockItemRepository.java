package com.example.support.inventory;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockItemRepository
        extends JpaRepository<StockItem, Long> {

    @EntityGraph(attributePaths = {"category"})
    List<StockItem> findByActiveTrueOrderByItemCodeAsc();

    List<StockItem>
    findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndItemCodeContainingIgnoreCase(
            String name,
            String itemCode
    );
}
