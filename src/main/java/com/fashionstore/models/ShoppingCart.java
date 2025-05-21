package com.fashionstore.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShoppingCart {
    private String cartId;
    private String userId;
    private List<CartItem> items;
    private BigDecimal totalPrice;

    public class CartItem {
        private Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            setQuantity(quantity); // Use the setter to enforce minimum quantity
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            // Validate and set quantity
            if (quantity < 1) {
                System.out.println("CartItem.setQuantity: Invalid quantity: " + quantity + ", setting to 1");
                quantity = 1; // Ensure quantity is at least 1
            }
            
            // Validate against stock limit
            if (product != null && quantity > product.getStockQuantity()) {
                int availableStock = product.getStockQuantity();
                System.out.println("CartItem.setQuantity: Quantity " + quantity + 
                                  " exceeds available stock of " + availableStock + 
                                  ", limiting to stock amount");
                quantity = availableStock;
            }
            
            // Check if quantity actually changed
            boolean changed = this.quantity != quantity;
            
            // Set the new quantity
            this.quantity = quantity;
            
            // Recalculate parent cart total if quantity changed
            if (changed) {
                System.out.println("CartItem.setQuantity: Quantity changed to: " + quantity + 
                                  " for product: " + (product != null ? product.getName() : "null"));
                ShoppingCart.this.recalculateTotal();
            }
        }

        public BigDecimal getTotalPrice() {
            if (product == null || product.getPrice() == null) {
                return BigDecimal.ZERO;
            }
            return product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
        
        @Override
        public String toString() {
            return "CartItem{" +
                   "product=" + (product != null ? product.getName() + " (ID: " + product.getProductId() + ")" : "null") +
                   ", quantity=" + quantity +
                   '}';
        }
    }

    public ShoppingCart(String userId) {
        this.cartId = UUID.randomUUID().toString();
        this.userId = userId;
        this.items = new ArrayList<>();
        this.totalPrice = BigDecimal.ZERO;
    }

    public void addItem(Product product) {
        addItem(product, 1);
    }

    public void addItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        
        // Validate that quantity is positive
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Check if product already exists in cart
        for (CartItem item : items) {
            if (item.getProduct().getProductId().equals(product.getProductId())) {
                // Calculate the new total quantity after adding
                int newQuantity = item.getQuantity() + quantity;
                
                // Ensure new quantity doesn't exceed available stock
                if (newQuantity > product.getStockQuantity()) {
                    // Set quantity to maximum available stock instead
                    newQuantity = product.getStockQuantity();
                    System.out.println("ShoppingCart.addItem: Limiting quantity to available stock: " + newQuantity);
                }
                
                item.setQuantity(newQuantity);
                recalculateTotal();
                return;
            }
        }

        // For new items, check if quantity exceeds stock before adding
        int adjustedQuantity = quantity;
        if (quantity > product.getStockQuantity()) {
            adjustedQuantity = product.getStockQuantity();
            System.out.println("ShoppingCart.addItem: Limiting new item quantity to available stock: " + adjustedQuantity);
        }

        // Add new item if product doesn't exist in cart and stock is available
        if (adjustedQuantity > 0) {
            items.add(new CartItem(product, adjustedQuantity));
            recalculateTotal();
        }
    }

    /**
     * Removes an item completely from the cart, regardless of quantity.
     * Ensures the item is fully removed by working with product IDs.
     * 
     * @param productId The ID of the product to remove
     * @return true if the item was removed, false otherwise
     */
    public boolean removeItem(String productId) {
        if (productId == null || productId.isEmpty()) {
            System.err.println("Cannot remove item: productId is null or empty");
            return false;
        }

        System.out.println("ShoppingCart.removeItem: Starting removal of product ID: " + productId);
        
        // Get the initial size for validation
        int initialSize = items.size();
        System.out.println("ShoppingCart.removeItem: Cart has " + initialSize + " items before removal");
        
        // Create a new list without the item to ensure complete removal
        List<CartItem> updatedItems = new ArrayList<>();
        boolean found = false;
        
        for (CartItem item : items) {
            if (item != null && item.getProduct() != null && 
                productId.equals(item.getProduct().getProductId())) {
                // Skip this item (removing it)
                found = true;
                System.out.println("ShoppingCart.removeItem: Found item to remove - " + 
                                  item.getProduct().getName() + " (quantity: " + item.getQuantity() + ")");
            } else {
                // Keep this item
                updatedItems.add(item);
            }
        }
        
        // Replace the items list with our filtered list
        items.clear();
        items.addAll(updatedItems);
        
        // Recalculate cart total after removal
        if (found) {
            System.out.println("ShoppingCart.removeItem: Item successfully removed");
            recalculateTotal();
        } else {
            System.out.println("ShoppingCart.removeItem: No matching item found with ID: " + productId);
        }
        
        return found;
    }

    public void clear() {
        items.clear();
        totalPrice = BigDecimal.ZERO;
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public BigDecimal getTotalPrice() {
        recalculateTotal();
        return totalPrice;
    }

    public int getItemCount() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    private void recalculateTotal() {
        totalPrice = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters
    public String getCartId() {
        return cartId;
    }

    // Add setter for cartId to support database loading
    public void setCartId(String cartId) {
        if (cartId != null) {
            this.cartId = cartId;
        }
    }

    public String getUserId() {
        return userId;
    }
}