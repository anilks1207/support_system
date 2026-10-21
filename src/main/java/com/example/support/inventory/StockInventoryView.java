package com.example.support.inventory;

import java.util.List;
import com.example.support.inventory.Location;
import com.example.support.inventory.StockBalance;
import com.example.support.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Route(value = "stock-inventory", layout = MainLayout.class)
@PageTitle("Stock Inventory")
@PermitAll
public class StockInventoryView extends VerticalLayout {

    private final StockItemRepository stockItemRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final StockCategoryRepository stockCategoryRepository;
    private final LocationRepository locationRepository;
    private final StockAdjustmentService stockAdjustmentService;

    private final Grid<StockItem> grid = new Grid<>(StockItem.class, false);
    {
        grid.setHeight("350px");
    }

    private final TextField searchField = new TextField("Search items");
    private final ComboBox<String> categoryFilter =
            new ComboBox<>("Category");

    private final VerticalLayout editor = new VerticalLayout();

    private final TextField itemCodeField = new TextField("Item Code");
    private final TextField nameField = new TextField("Item Name");
    private final ComboBox<StockCategory> categoryField =
            new ComboBox<>("Category");
    private final TextField unitField = new TextField("Unit");

    private final NumberField thresholdField =
            new NumberField("Low-Stock Threshold");

    private final ComboBox<Location> locationField =
            new ComboBox<>("Stock Location");

    private final NumberField quantityField =
            new NumberField("Stock Quantity");

    private final Checkbox activeField =
            new Checkbox("Active");

    private final Button addButton = new Button("Add Item");
    private final Button editButton = new Button("Edit Selected");
    private final Button saveButton = new Button("Save Item");
    private final Button cancelButton = new Button("Cancel");

    private StockItem editingItem;

    public StockInventoryView(
            StockItemRepository stockItemRepository,
            StockBalanceRepository stockBalanceRepository,
            StockCategoryRepository stockCategoryRepository,
            LocationRepository locationRepository,
            StockAdjustmentService stockAdjustmentService) {

        this.stockItemRepository = stockItemRepository;
        this.stockBalanceRepository = stockBalanceRepository;
        this.stockCategoryRepository = stockCategoryRepository;
        this.locationRepository = locationRepository;
        this.stockAdjustmentService = stockAdjustmentService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("overflow-y", "auto");

        H2 heading = new H2("Stock Inventory");
        heading.getStyle()
                .set("color", "#174EA6")
                .set("margin", "0");

        searchField.setPlaceholder("Search by item name or code");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("320px");

        categoryFilter.setItems(
                "Electrical", "Plumbing", "Housekeeping", "Painting",
                "RO Plant", "Heat Pump", "STP Plant", "Other");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setWidth("230px");

        grid.addColumn(StockItem::getItemCode)
                .setHeader("Item Code").setAutoWidth(true);

        grid.addColumn(StockItem::getName)
                .setHeader("Item Name").setAutoWidth(true).setFlexGrow(1);

        grid.addColumn(item -> item.getCategory() == null
                        ? "" : item.getCategory().getName())
                .setHeader("Category").setAutoWidth(true);

        grid.addColumn(StockItem::getUnit)
                .setHeader("Unit").setAutoWidth(true);

        grid.addColumn(item ->
                        item.getLowStockThreshold()
                                .stripTrailingZeros().toPlainString())
                .setHeader("Low-Stock Threshold").setAutoWidth(true);

        grid.addColumn(this::getAvailableQuantity)
                .setHeader("Available Quantity").setAutoWidth(true);

        grid.addColumn(this::getLocationName)
                .setHeader("Location").setAutoWidth(true);

        grid.setWidthFull();
        grid.setMinHeight("250px");
        grid.setHeight("350px");
        grid.getStyle().set("flexShrink", "0");

        categoryField.setLabel("Category");
        categoryField.setItems(
                stockCategoryRepository.findByActiveTrueOrderByNameAsc());
        categoryField.setItemLabelGenerator(StockCategory::getName);
        categoryField.setWidthFull();

        locationField.setLabel("Stock Location");
        locationField.setItems(
                locationRepository.findByActiveTrueOrderByNameAsc());
        locationField.setItemLabelGenerator(Location::getName);
        locationField.setWidthFull();
        locationField.setRequiredIndicatorVisible(true);

        quantityField.setLabel("Stock Quantity");
        quantityField.setMin(0);
        quantityField.setStep(0.001);
        quantityField.setRequiredIndicatorVisible(true);
        quantityField.setWidthFull();


        itemCodeField.setRequired(true);
        nameField.setRequired(true);
        categoryField.setRequired(true);
        unitField.setRequired(true);
        thresholdField.setMin(0);
        thresholdField.setStep(1);
        thresholdField.setRequiredIndicatorVisible(true);

        HorizontalLayout fieldsRow1 =
                new HorizontalLayout(itemCodeField, nameField);
        fieldsRow1.setWidthFull();

        HorizontalLayout fieldsRow2 =
                new HorizontalLayout(categoryField, unitField);
        fieldsRow2.setWidthFull();

        HorizontalLayout fieldsRow3 =
                new HorizontalLayout(locationField, quantityField);
        fieldsRow3.setWidthFull();


        HorizontalLayout formButtons =
                new HorizontalLayout(saveButton, cancelButton);

        editor.add(
                new H2("Item Details"),
                fieldsRow1,
                fieldsRow2,
                fieldsRow3,
                thresholdField,
                activeField,
                formButtons
        );

        editor.setWidthFull();
        editor.setPadding(true);
        editor.setSpacing(false);
        editor.getStyle().set("flexShrink", "0");
        editor.getStyle()
                .set("background-color", "#F1F6FE")
                .set("border", "1px solid #D6E4F5")
                .set("border-radius", "10px");

        editor.setVisible(false);

        addButton.setVisible(canManageStock());
        editButton.setVisible(canManageStock());

        grid.asSingleSelect().addValueChangeListener(event ->
                editButton.setEnabled(
                        canManageStock() && event.getValue() != null));

        addButton.getStyle()
                .set("background-color", "#174EA6")
                .set("color", "white");

        addButton.addClickListener(event -> openNewItemForm());
        editButton.addClickListener(event -> openEditItemForm());
        saveButton.addClickListener(event -> saveItem());
        cancelButton.addClickListener(event -> closeEditor());

        grid.asSingleSelect().addValueChangeListener(event ->
                editButton.setEnabled(
                        canManageStock() && event.getValue() != null));

        searchField.addValueChangeListener(event -> refreshGrid());
        categoryFilter.addValueChangeListener(event -> refreshGrid());

        add(
                heading,
                new HorizontalLayout(searchField, categoryFilter),
                new HorizontalLayout(addButton, editButton),
                editor,
                grid
        );

        editButton.setEnabled(false);
        refreshGrid();
    }

    private boolean canManageStock() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities().stream().anyMatch(authority -> {
            String role = authority.getAuthority();
            return role.equals("ROLE_SUPPORT")
                    || role.equals("SUPPORT")
                    || role.equals("ROLE_ADMIN")
                    || role.equals("ADMIN");
        });
    }

    private void openNewItemForm() {
        if (!canManageStock()) {
            Notification.show("You do not have permission to add items.");
            return;
        }

        editingItem = null;
        itemCodeField.clear();
        nameField.clear();
        categoryField.clear();
        unitField.clear();
        thresholdField.clear();

        locationField.clear();
        quantityField.clear();

        activeField.setValue(true);
        itemCodeField.setReadOnly(false);
        editor.setVisible(true);
    }

    private void openEditItemForm() {
        if (!canManageStock()) {
            Notification.show("You do not have permission to edit items.");
            return;
        }

        StockItem selected = grid.asSingleSelect().getValue();

        if (selected == null) {
            Notification.show("Please select an item first.");
            return;
        }

        editingItem = selected;

        itemCodeField.setValue(selected.getItemCode());
        nameField.setValue(selected.getName());
        categoryField.setValue(selected.getCategory());
        unitField.setValue(selected.getUnit());
        thresholdField.setValue(
                selected.getLowStockThreshold().doubleValue());
        activeField.setValue(selected.isActive());

        locationField.clear();
        quantityField.clear();

        List<StockBalance> balances =
                stockBalanceRepository.findAll().stream()
                        .filter(b -> b.getStockItem().getId()
                                .equals(selected.getId()))
                        .toList();


        if (!balances.isEmpty()) {
            StockBalance balance = balances.get(0);

            Long locationId = balance.getLocation().getId();

            Location matchingLocation = locationField
                    .getListDataView()
                    .getItems()
                    .filter(location -> location.getId().equals(locationId))
                    .findFirst()
                    .orElse(null);

            if (matchingLocation != null) {
                locationField.setValue(matchingLocation);
            }

            quantityField.setValue(
                    balance.getQuantityOnHand().doubleValue());
        }


        itemCodeField.setReadOnly(true);
        editor.setVisible(true);
    }

    private void saveItem() {
        if (!canManageStock()) {
            Notification.show("You do not have permission to change items.");
            return;
        }

        if (itemCodeField.isEmpty()
                || nameField.isEmpty()
                || categoryField.isEmpty()
                || unitField.isEmpty()
                || thresholdField.isEmpty()
                || locationField.isEmpty()
                || quantityField.isEmpty()) {
            Notification.show("Please complete all required fields.");
            return;
        }

        double threshold = thresholdField.getValue();

        if (!Double.isFinite(threshold)
                || threshold < 0
                || threshold != Math.floor(threshold)) {
            Notification.show(
                    "Low-stock threshold must be a whole number of zero or more.");
            return;
        }

        // ADD THE NEW QUANTITY VALIDATION HERE
        double quantity = quantityField.getValue();

        if (!Double.isFinite(quantity) || quantity < 0
                || BigDecimal.valueOf(quantity).stripTrailingZeros().scale() > 3) {
            Notification.show(
                    "Stock quantity must be zero or greater, with at most 3 decimal places.");
            return;
        }

        try {
            OffsetDateTime now = OffsetDateTime.now();

            StockItem item;

            if (editingItem == null) {
                item = new StockItem();
                item.setItemCode(itemCodeField.getValue().trim());
                item.setCreatedAt(now);
            } else {
                item = stockItemRepository.findById(editingItem.getId())
                        .orElseThrow();
            }

            item.setName(nameField.getValue().trim());
            item.setCategory(categoryField.getValue());
            item.setUnit(unitField.getValue().trim());
            item.setLowStockThreshold(
                    BigDecimal.valueOf(threshold).setScale(2));
            item.setActive(activeField.getValue());
            item.setUpdatedAt(now);

            stockItemRepository.saveAndFlush(item);

            stockAdjustmentService.adjustStock(
                    item.getId(),
                    locationField.getValue().getId(),
                    BigDecimal.valueOf(quantityField.getValue()),
                    "Stock quantity updated from inventory screen"
            );

            Notification.show(
                    editingItem == null
                            ? "Stock item added successfully."
                            : "Stock item updated successfully.");

            closeEditor();
            refreshGrid();


        } catch (Exception exception) {
            exception.printStackTrace();

            Notification.show(
                    "Could not save item: " + exception.getMessage());
        }

}

    private void closeEditor() {
        editor.setVisible(false);
        editingItem = null;
        itemCodeField.setReadOnly(false);
    }

    private String getAvailableQuantity(StockItem item) {
        return stockBalanceRepository
                .findByLocation_CodeOrderByStockItem_ItemCodeAsc("STORE")
                .stream()
                .filter(balance -> balance.getStockItem() != null
                        && balance.getStockItem().getId().equals(item.getId()))
                .map(balance -> balance.getAvailableQuantity()
                        .stripTrailingZeros().toPlainString())
                .findFirst()
                .orElse("0");
    }

    private String getLocationName(StockItem item) {
        return stockBalanceRepository
                .findByLocation_CodeOrderByStockItem_ItemCodeAsc("STORE")
                .stream()
                .filter(balance -> balance.getStockItem() != null
                        && balance.getStockItem().getId().equals(item.getId()))
                .map(balance -> balance.getLocation().getName())
                .findFirst()
                .orElse("Central Store");
    }

    private void refreshGrid() {
        String search = searchField.getValue() == null
                ? ""
                : searchField.getValue().trim().toLowerCase();

        String selectedCategory = categoryFilter.getValue();

        List<StockItem> items =
                stockItemRepository.findByActiveTrueOrderByItemCodeAsc();

        List<StockItem> filtered = items.stream()
                .filter(item -> search.isEmpty()
                        || item.getName().toLowerCase().contains(search)
                        || item.getItemCode().toLowerCase().contains(search))
                .filter(item -> selectedCategory == null
                        || (item.getCategory() != null
                        && selectedCategory.equals(
                        item.getCategory().getName())))
                .toList();

        grid.setItems(filtered);
    }
}
