
package com.example.support.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "stock_balances")
public class StockBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "quantity_on_hand", nullable = false,
            precision = 19, scale = 3)
    private BigDecimal quantityOnHand;

    @Column(name = "quantity_reserved", nullable = false,
            precision = 19, scale = 3)
    private BigDecimal quantityReserved;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected StockBalance() {
    }

    public Long getId() {
        return id;
    }

    public StockItem getStockItem() {
        return stockItem;
    }

    public Location getLocation() {
        return location;
    }

    public BigDecimal getQuantityOnHand() {
        return quantityOnHand;
    }

    public BigDecimal getQuantityReserved() {
        return quantityReserved;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public BigDecimal getAvailableQuantity() {
        return quantityOnHand.subtract(quantityReserved);
    }

    public void setStockItem(StockItem stockItem) {
        this.stockItem = stockItem;
    }

    public void setQuantityReserved(BigDecimal quantityReserved) {
        this.quantityReserved = quantityReserved;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setQuantityOnHand(BigDecimal quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
