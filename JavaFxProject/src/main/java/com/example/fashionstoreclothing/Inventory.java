package com.example.fashionstoreclothing;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Inventory implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<ClothingItem> items;

    // Transient ObservableList - won't be serialized
    private transient ObservableList<ClothingItem> observableItems;

    public Inventory() {
        this.items = new ArrayList<>();
    }

    public List<ClothingItem> getItems() {
        return items;
    }

    public void setItems(List<ClothingItem> items) {
        this.items = items;
    }

    // Method to get items as ObservableList for JavaFX UI binding
    public ObservableList<ClothingItem> getObservableItems() {
        if (observableItems == null) {
            observableItems = FXCollections.observableArrayList(items);
        } else {
            observableItems.setAll(items);
        }
        return observableItems;
    }

    public void addItem(ClothingItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        items.add(item);

        // Update observable list if it exists
        if (observableItems != null) {
            observableItems.add(item);
        }
    }

    public void removeItem(ClothingItem item) {
        items.remove(item);

        // Update observable list if it exists
        if (observableItems != null) {
            observableItems.remove(item);
        }
    }

    // Fixed method that accepts one parameter
    public void updateItem(ClothingItem updatedItem) {
        if (updatedItem == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        for (int i = 0; i < items.size(); i++) {
            ClothingItem item = items.get(i);
            if (item.getId() == updatedItem.getId()) {
                items.set(i, updatedItem);

                // Update observable list if it exists
                if (observableItems != null) {
                    observableItems.set(i, updatedItem);
                }
                return;
            }
        }

        // If item not found, add it
        addItem(updatedItem);
    }

    // Method that accepts two parameters - original item and updated item
    public void updateItem(ClothingItem originalItem, ClothingItem updatedItem) {
        if (originalItem == null || updatedItem == null) {
            throw new IllegalArgumentException("Both original and updated items cannot be null");
        }

        int index = items.indexOf(originalItem);
        if (index != -1) {
            items.set(index, updatedItem);

            // Update observable list if it exists
            if (observableItems != null) {
                observableItems.set(index, updatedItem);
            }
        } else {
            // If the original item is not found, just add the updated item
            addItem(updatedItem);
        }
    }

    public ClothingItem findItemById(int id) {
        for (ClothingItem item : items) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    public List<ClothingItem> findItemsByCategory(String category) {
        return items.stream()
                .filter(item -> item.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(ClothingItem::getQuantity)
                .sum();
    }
}