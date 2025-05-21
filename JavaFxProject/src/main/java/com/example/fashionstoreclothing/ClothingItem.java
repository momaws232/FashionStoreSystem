package com.example.fashionstoreclothing;

import java.io.Serializable;

public class ClothingItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int LOW_STOCK_THRESHOLD = 5; // Define what counts as "low stock"

    private int id;
    private String name;
    private String category;
    private String size;
    private String color;
    private double price;
    private int quantity;

    public ClothingItem(int id, String name, String category, String size, String color, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.size = size;
        this.color = color;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Missing methods that need to be added

    /**
     * Check if the item is considered low stock
     * @return true if quantity is below the threshold
     */
    public boolean isLowStock() {
        return quantity <= LOW_STOCK_THRESHOLD;
    }

    /**
     * Calculate the total value of this item (price × quantity)
     * @return the total value
     */
    public double getTotalValue() {
        return price * quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClothingItem that = (ClothingItem) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}