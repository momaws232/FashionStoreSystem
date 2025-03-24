package com.example.fashionstoreclothing;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Inventory implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<ClothingItem> items;

    public Inventory() {
        this.items = new ArrayList<>();
    }

    public void addItem(ClothingItem item) {
        items.add(item);
    }

    public void removeItem(ClothingItem item) {
        items.remove(item);
    }

    public void updateItem(ClothingItem oldItem, ClothingItem newItem) {
        int index = items.indexOf(oldItem);
        if (index != -1) {
            items.set(index, newItem);
        }
    }

    public List<ClothingItem> getItems() {
        return items;
    }

    public ObservableList<ClothingItem> getObservableItems() {
        return FXCollections.observableArrayList(items);
    }

    public void setItems(List<ClothingItem> items) {
        this.items = items;
    }
}