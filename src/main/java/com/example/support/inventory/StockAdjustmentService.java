package com.example.support.inventory;

import com.example.support.user.User;
import com.example.support.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class StockAdjustmentService {

    private final StockBalanceRepository balanceRepository;
    private final StockTransactionRepository transactionRepository;
    private final StockItemRepository itemRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public StockAdjustmentService(
            StockBalanceRepository balanceRepository,
            StockTransactionRepository transactionRepository,
            StockItemRepository itemRepository,
            LocationRepository locationRepository,
            UserRepository userRepository) {

        this.balanceRepository = balanceRepository;
        this.transactionRepository = transactionRepository;
        this.itemRepository = itemRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void adjustStock(
            Long itemId,
            Long locationId,
            BigDecimal newQuantity,
            String note) {

        User user = getAuthorizedUser();

        if (newQuantity == null || newQuantity.signum() < 0
                || newQuantity.scale() > 3) {
            throw new IllegalArgumentException(
                    "Quantity must be zero or greater, with at most 3 decimal places.");
        }

        StockItem item = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Stock item not found."));

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Location not found."));

        StockBalance balance = balanceRepository
                .findByStockItem_IdAndLocation_Id(itemId, locationId)
                .orElseGet(() -> {
                    StockBalance newBalance = new StockBalance();
                    newBalance.setStockItem(item);
                    newBalance.setLocation(location);
                    newBalance.setQuantityOnHand(BigDecimal.ZERO);
                    newBalance.setQuantityReserved(BigDecimal.ZERO);
                    newBalance.setUpdatedAt(OffsetDateTime.now());
                    return newBalance;
                });

        BigDecimal oldQuantity = balance.getQuantityOnHand();
        BigDecimal difference = newQuantity.subtract(oldQuantity);

        if (difference.signum() == 0) {
            return;
        }

        if (newQuantity.compareTo(balance.getQuantityReserved()) < 0) {
            throw new IllegalArgumentException(
                    "Quantity cannot be less than the reserved quantity.");
        }

        balance.setQuantityOnHand(newQuantity);
        balance.setUpdatedAt(OffsetDateTime.now());
        balanceRepository.save(balance);

        StockTransaction transaction = new StockTransaction();
        transaction.setStockItem(item);
        transaction.setLocation(location);

        transaction.setTransactionType("ADJUSTMENT");
        transaction.setQuantityChange(difference);
        transaction.setPerformedBy(user.getId());
        transaction.setCreatedAt(OffsetDateTime.now());
        transaction.setNote(note == null || note.isBlank()
                ? "Stock quantity adjustment"
                : note.trim());

        transactionRepository.save(transaction);
    }

    private User getAuthorizedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Please sign in first.");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new AccessDeniedException("User account not found."));

        String role = user.getRole();

        if (!"SUPPORT".equalsIgnoreCase(role)
                && !"ADMIN".equalsIgnoreCase(role)) {
            throw new AccessDeniedException(
                    "Only support staff and administrators can change stock.");
        }

        return user;
    }
}
