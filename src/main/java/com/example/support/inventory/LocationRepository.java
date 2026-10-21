package com.example.support.inventory;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @EntityGraph(attributePaths = {})
    List<Location> findByActiveTrueOrderByNameAsc();
}
