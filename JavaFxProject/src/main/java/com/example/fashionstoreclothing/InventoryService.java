package com.example.fashionstoreclothing;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class InventoryService {
    private static final String DATA_FILE = "inventory.dat";
    private List<InventoryItem> items;
    private AtomicInteger idCounter = new AtomicInteger(1);

    public InventoryService() {
        items = loadItems();

        // Set the ID counter to the highest ID + 1
        items.forEach(item -> {
            if (item.getId() >= idCounter.get()) {
                idCounter.set(item.getId() + 1);
            }
        });
    }

    public List<InventoryItem> getAllItems() {
        return new ArrayList<>(items);
    }

    public Optional<InventoryItem> getItemById(int id) {
        return items.stream()
                .filter(item -> item.getId() == id)
                .findFirst();
    }

    public void addItem(InventoryItem item) {
        // Set a new ID for the item
        item.setId(idCounter.getAndIncrement());
        items.add(item);
        saveItems();
    }

    public void updateItem(InventoryItem updatedItem) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == updatedItem.getId()) {
                items.set(i, updatedItem);
                saveItems();
                return;
            }
        }
    }

    public boolean deleteItem(int id) {
        boolean removed = items.removeIf(item -> item.getId() == id);
        if (removed) {
            saveItems();
        }
        return removed;
    }

    private List<InventoryItem> loadItems() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            return (List<InventoryItem>) in.readObject();
        } catch (FileNotFoundException e) {
            // Return empty list if file doesn't exist yet
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading inventory: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveItems() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(items);
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
        }
    }
}