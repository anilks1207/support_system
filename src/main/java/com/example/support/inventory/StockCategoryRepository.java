
package com.example.support.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockCategoryRepository
        extends JpaRepository<StockCategory, Long> {

    List<StockCategory> findByActiveTrueOrderByNameAsc();
}
