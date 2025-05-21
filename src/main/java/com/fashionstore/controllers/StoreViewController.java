package com.fashionstore.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.fashionstore.application.FashionStoreApp;
import com.fashionstore.models.Product;
import com.fashionstore.storage.DataManager;
import com.fashionstore.ui.components.StoreItemView;
import com.fashionstore.utils.SceneManager;
import com.fashionstore.utils.WindowManager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class StoreViewController implements Initializable {

    @FXML
    private FlowPane storeItemsPane;
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private Label itemCountLabel; // Optional - can add this to your FXML if desired

    private DataManager dataManager;
    private List<Product> storeItems;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dataManager = FashionStoreApp.getDataManager();

        if (dataManager.getCurrentUser() == null) {
            SceneManager.loadScene("LoginView.fxml");
            return;
        }

        // Force reload all data to ensure we have the latest
        dataManager.loadAllData();

        // Set ID and userData for window refresh support
        javafx.application.Platform.runLater(() -> {
            if (storeItemsPane.getScene() != null) {
                storeItemsPane.getScene().getRoot().setId("storeView");
                storeItemsPane.getScene().getRoot().setUserData(this);
            }
        });

        // Load store items (only visible products)
        loadVisibleProducts();
        
        // Set up filters
        setupFilters();

        // Display items
        displayStoreItems();
    }
    
    /**
     * Load the most up-to-date visible products from the database
     */
    private void loadVisibleProducts() {
        // Force reload all data from the database to ensure latest visibility status
        dataManager.loadAllData();
        
        // Get fresh product list (only visible)
        storeItems = dataManager.getVisibleProducts();
        System.out.println("StoreViewController: Loaded " + storeItems.size() + " visible products");
    }

    private void setupFilters() {
        // Add categories
        categoryFilter.getItems().add("All Categories");

        // Get unique categories
        storeItems.stream()
                .map(Product::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted() // Sort categories alphabetically
                .forEach(category -> categoryFilter.getItems().add(category));

        categoryFilter.setValue("All Categories");
        categoryFilter.setOnAction(e -> displayStoreItems());
    }

    private void displayStoreItems() {
        // Clear existing items
        storeItemsPane.getChildren().clear();
        
        if (storeItems.isEmpty()) {
            Label noItemsLabel = new Label("No items available in this category.");
            noItemsLabel.getStyleClass().add("no-items-label");
            storeItemsPane.getChildren().add(noItemsLabel);
            if (itemCountLabel != null) {
                itemCountLabel.setText("Showing 0 items");
            }
            return;
        }

        // Apply category filter if selected
        String category = categoryFilter.getValue();
        List<Product> filteredItems = storeItems;
        
        if (category != null && !category.equals("All Categories")) {
            filteredItems = storeItems.stream()
                    .filter(item -> category.equals(item.getCategory()))
                    .collect(Collectors.toList());
        }

        // Check if filtered list is empty
        if (filteredItems.isEmpty()) {
            Label noItemsLabel = new Label("No items available in this category.");
            noItemsLabel.getStyleClass().add("no-items-label");
            storeItemsPane.getChildren().add(noItemsLabel);
            if (itemCountLabel != null) {
                itemCountLabel.setText("Showing 0 items");
            }
            return;
        }

        // Create store item view components
        for (Product product : filteredItems) {
            // Skip items that aren't visible (additional safety check)
            if (!product.isVisible()) {
                continue;
            }
            
            StoreItemView itemView = new StoreItemView(product);
            itemView.setOnPurchase(e -> handlePurchase(product));
            // Make items fully visible immediately with animation class
            itemView.setOpacity(1);
            itemView.getStyleClass().add("item-fade-in");
            storeItemsPane.getChildren().add(itemView);
        }

        // Update count label if it exists
        if (itemCountLabel != null) {
            itemCountLabel.setText("Showing " + filteredItems.size() + " items");
        }
    }

    private void handlePurchase(Product product) {
        // Check if item is already in wardrobe
        List<Product> wardrobe = dataManager.getUserWardrobe(dataManager.getCurrentUser().getUserId());
        boolean alreadyOwned = wardrobe.stream()
                .anyMatch(p -> p.getProductId().equals(product.getProductId()));

        if (alreadyOwned) {
            SceneManager.showAlert("Already Owned",
                    "You already have this item in your wardrobe.");
            return;
        }

        // Check if item is in stock
        if (product.getStockQuantity() <= 0) {
            SceneManager.showAlert("Out of Stock",
                    "Sorry, this item is currently out of stock.");
            return;
        }

        // Make sure the product has an image path before adding to wardrobe
        if (product.getImagePath() == null || product.getImagePath().isEmpty()) {
            product.setImagePath("/images/default-product.jpg");
        }

        // Decrease stock quantity by 1
        product.setStockQuantity(product.getStockQuantity() - 1);

        // Update product in database to reflect stock change
        dataManager.updateProduct(product);

        // Add to user's wardrobe
        dataManager.getCurrentUser().addToWardrobe(product.getProductId());

        // Important: Save data after modifications
        dataManager.saveAllData();

        SceneManager.showAlert("Purchase Successful",
                "Item added to your wardrobe: " + product.getName());

        // Refresh the current view
        refreshView();

        // Refresh other open views
        WindowManager.refreshHomeView();
    }

    // Refresh the view (can be called from outside)
    public void refreshView() {
        System.out.println("StoreViewController: Refreshing view");
        
        // Get fresh product list with up-to-date visibility status
        loadVisibleProducts();
        
        // Debug visibility of products
        logProductVisibility();
        
        System.out.println("StoreViewController: Refreshed with " + storeItems.size() + " products");
        setupFilters();
        displayStoreItems();
    }
    
    /**
     * Log visibility status of products for debugging
     */
    private void logProductVisibility() {
        // Get all products to check visibility status
        List<Product> allProducts = dataManager.getAllProducts();
        List<Product> visibleProducts = dataManager.getVisibleProducts();
        
        System.out.println("VISIBILITY STATUS: Total products: " + allProducts.size() + 
                          ", Visible products: " + visibleProducts.size() + 
                          ", Hidden products: " + (allProducts.size() - visibleProducts.size()));
        
        // Log hidden products for debugging
        if (allProducts.size() > visibleProducts.size()) {
            System.out.println("Hidden products:");
            allProducts.stream()
                .filter(p -> !p.isVisible())
                .forEach(p -> System.out.println("  - " + p.getName() + " (ID: " + p.getProductId() + ")"));
        }
    }
}