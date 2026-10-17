package com.example.support.inventory;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "stock_transactions")
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "quantity_change", nullable = false,
            precision = 19, scale = 3)
    private BigDecimal quantityChange;

    @Column(name = "performed_by", nullable = false)
    private Long performedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "note")
    private String note;

    protected StockTransaction() {
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

    public String getTransactionType() {
        return transactionType;
    }

    public BigDecimal getQuantityChange() {
        return quantityChange;
    }

    public Long getPerformedBy() {
        return performedBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public String getNote() {
        return note;
    }

    public void setStockItem(StockItem stockItem) {
        this.stockItem = stockItem;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public void setQuantityChange(BigDecimal quantityChange) {
        this.quantityChange = quantityChange;
    }

    public void setPerformedBy(Long performedBy) {
        this.performedBy = performedBy;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
