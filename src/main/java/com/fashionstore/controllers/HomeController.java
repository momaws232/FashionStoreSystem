package com.fashionstore.controllers;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.fashionstore.application.FashionStoreApp;
import com.fashionstore.models.Product;
import com.fashionstore.models.ShoppingCart;
import com.fashionstore.models.User;
import com.fashionstore.storage.DataManager;
import com.fashionstore.ui.components.StoreItemView;
import com.fashionstore.utils.SceneManager;
import com.fashionstore.utils.WindowManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class HomeController implements Initializable {
    @FXML
    private Button cartButton;
    @FXML
    private Label userLabel;
    @FXML
    private Button wardrobeButton;
    @FXML
    private Button outfitsButton;
    @FXML
    private Button aiButton;
    @FXML
    private Button logoutButton;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private ComboBox<String> priceFilter;
    @FXML
    private Button searchButton;
    @FXML
    private FlowPane storeItemsPane;
    @FXML
    private Label itemCountLabel;
    @FXML
    private Button themeToggleButton;

    private DataManager dataManager;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dataManager = FashionStoreApp.getDataManager();
        currentUser = dataManager.getCurrentUser();

        Platform.runLater(() -> {
            if (storeItemsPane.getScene() != null) {
                storeItemsPane.getScene().getRoot().setId("homeView");
                storeItemsPane.getScene().getRoot().setUserData(this);
                
                // Set the initial theme toggle button text based on current theme
                String currentTheme = "light";
                if (storeItemsPane.getScene().getRoot().getProperties().get("theme") != null) {
                    currentTheme = (String) storeItemsPane.getScene().getRoot().getProperties().get("theme");
                }
                themeToggleButton.setText(currentTheme.equals("dark") ? "Light Mode" : "Dark Mode");
            }
        });

        if (currentUser == null) {
            SceneManager.loadScene("LoginView.fxml");
            return;
        }

        userLabel.setText("Welcome, " + currentUser.getUsername());

        // Get ALL products, not just visible ones, to ensure filters work on everything
        List<Product> storeItems = dataManager.getAllProducts();
        if (storeItems.isEmpty()) {
            storeItems = new ArrayList<>();
        }

        setupFilters(storeItems);
        displayStoreItems(storeItems);

        // Ensure filtering happens when value changes
        categoryFilter.setOnAction(e -> handleSearch());
        priceFilter.setOnAction(e -> handleSearch());

        Platform.runLater(this::maximizeWindow);
        Platform.runLater(this::setupResponsiveLayout);
        optimizeLayoutForMaximizedWindow();
    }

    private void setupResponsiveLayout() {
        storeItemsPane.prefWrapLengthProperty().bind(
                storeItemsPane.widthProperty());

        storeItemsPane.getScene().widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            if (width > 1600) {
                storeItemsPane.setHgap(25);
                storeItemsPane.setPrefWidth(width - 40);
            } else {
                storeItemsPane.setHgap(20);
            }
        });
    }

    private void maximizeWindow() {
        try {
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setMaximized(true);
            storeItemsPane.getParent().getStyleClass().add("page-transition");
        } catch (Exception e) {
            System.err.println("Failed to maximize window: " + e.getMessage());
        }
    }

    /**
     * Initializes the category and price filters when the product list is loaded.
     * Makes sure the filters work correctly by setting proper event handlers.
     */
    private void setupFilters(List<Product> storeItems) {
        // Clear existing items to avoid duplicates
        categoryFilter.getItems().clear();
        categoryFilter.getItems().add("All Categories");
        categoryFilter.setValue("All Categories");
        
        if (priceFilter.getItems().isEmpty()) {
            priceFilter.getItems().addAll(
                    "All Prices",
                    "Under $50",
                    "$50 - $100",
                    "$100 - $200",
                    "Over $200");
            priceFilter.setValue("All Prices");
        }

        // Add unique categories from the product list
        storeItems.stream()
            .map(Product::getCategory)
            .distinct()
            .sorted()
            .forEach(category -> {
                if (!categoryFilter.getItems().contains(category)) {
                    categoryFilter.getItems().add(category);
                }
            });
        
        // Connect filters directly to the filterItems method
        // This is more reliable than using the handleSearch method
        categoryFilter.setOnAction(e -> filterItems());
        priceFilter.setOnAction(e -> filterItems());
        
        System.out.println("Filters configured with direct connection to filterItems method");
    }

    private void displayStoreItems(List<Product> items) {
        storeItemsPane.getChildren().clear();
        itemCountLabel.setText("Showing " + items.size() + " items");

        // Prepare all item views
        List<StoreItemView> itemViews = new ArrayList<>();
        for (Product item : items) {
            StoreItemView itemView = new StoreItemView(item);
            itemView.setOnPurchase(e -> handlePurchase(item));
            // Make items fully visible immediately instead of starting with opacity 0
            itemView.setOpacity(1);
            // Add animation class that includes transform and other effects immediately
            itemView.getStyleClass().add("item-fade-in");
            itemViews.add(itemView);
        }

        // Add all items to the flow pane (already visible)
        storeItemsPane.getChildren().addAll(itemViews);
    }

    private void optimizeLayoutForMaximizedWindow() {
        storeItemsPane.getStyleClass().add("flow-pane-maximized");
        storeItemsPane.prefWrapLengthProperty().bind(
                storeItemsPane.widthProperty());

        storeItemsPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            if (width > 1600) {
                storeItemsPane.setHgap(30);
                storeItemsPane.setVgap(40);
            } else if (width > 1200) {
                storeItemsPane.setHgap(25);
                storeItemsPane.setVgap(30);
            } else {
                storeItemsPane.setHgap(20);
                storeItemsPane.setVgap(20);
            }
        });
    }

    /**
     * Handles the search event - filters items by search term, category, and price
     */
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().toLowerCase().trim();
        String selectedCategory = categoryFilter.getValue();
        String selectedPrice = priceFilter.getValue();

        System.out.println("Filtering with search: '" + searchTerm + 
                           "', category: '" + selectedCategory + 
                           "', price: '" + selectedPrice + "'");

        // IMPORTANT: Always get fresh products from the data manager to include both featured and regular items
        List<Product> allProducts = dataManager.getAllProducts();
        
        // Apply the same filtering logic as in the filterItems method
        List<Product> filteredItems = allProducts.stream()
                .filter(product -> {
                    // Search term filter
                    boolean matchesSearch = searchTerm.isEmpty() ||
                            product.getName().toLowerCase().contains(searchTerm) ||
                            product.getDescription().toLowerCase().contains(searchTerm) ||
                            product.getCategory().toLowerCase().contains(searchTerm);

                    // Category filter
                    boolean matchesCategory = "All Categories".equals(selectedCategory) ||
                            product.getCategory().equals(selectedCategory);

                    // Price filter
                    boolean matchesPrice;
                    switch (selectedPrice) {
                        case "Under $50":
                            matchesPrice = product.getPrice().compareTo(new BigDecimal("50")) < 0;
                            break;
                        case "$50 - $100":
                            matchesPrice = product.getPrice().compareTo(new BigDecimal("50")) >= 0 && 
                                          product.getPrice().compareTo(new BigDecimal("100")) <= 0;
                            break;
                        case "$100 - $200":
                            matchesPrice = product.getPrice().compareTo(new BigDecimal("100")) > 0 && 
                                          product.getPrice().compareTo(new BigDecimal("200")) <= 0;
                            break;
                        case "Over $200":
                            matchesPrice = product.getPrice().compareTo(new BigDecimal("200")) > 0;
                            break;
                        default: // "All Prices"
                            matchesPrice = true;
                            break;
                    }
                    return matchesSearch && matchesCategory && matchesPrice;
                })
                .collect(Collectors.toList());

        // Display the filtered items
        System.out.println("Search found " + filteredItems.size() + " items");
        displayStoreItems(filteredItems);
    }

    private void handlePurchase(Product product) {
        // Check if item is in stock
        if (product.getStockQuantity() <= 0) {
            SceneManager.showAlert("Out of Stock",
                    "Sorry, this item is currently out of stock.");
            return;
        }

        // Make sure the product has an image path before adding to cart
        if (product.getImagePath() == null || product.getImagePath().isEmpty()) {
            product.setImagePath("/images/default-product.jpg");
            // Update product in database to ensure image path is saved
            dataManager.updateProduct(product);
        }

        ShoppingCart cart = dataManager.getCart(currentUser.getUserId());
        
        // Get current quantity in cart before adding
        int currentQuantity = 0;
        for (ShoppingCart.CartItem item : cart.getItems()) {
            if (item.getProduct().getProductId().equals(product.getProductId())) {
                currentQuantity = item.getQuantity();
                break;
            }
        }
        
        // Check if adding would exceed stock
        if (currentQuantity >= product.getStockQuantity()) {
            SceneManager.showAlert("Maximum Stock Reached",
                    "You already have all available items (" + product.getStockQuantity() + 
                    ") of this product in your cart.");
            return;
        }
        
        // Add to cart (ShoppingCart.addItem will handle stock validation)
        cart.addItem(product);
        dataManager.saveCart(cart);
        dataManager.saveAllData();
        
        // Check if we were limited by stock
        if (currentQuantity + 1 >= product.getStockQuantity()) {
            SceneManager.showAlert("Added to Cart - Stock Limit Reached",
                    product.getName() + " has been added to your cart. You now have the maximum available quantity.");
        } else {
            SceneManager.showAlert("Added to Cart",
                    product.getName() + " has been added to your shopping cart.");
        }

        // Refresh all open views
        WindowManager.refreshHomeView();
    }

    @FXML
    private void openWardrobe() {
        WindowManager.openWardrobeWindow();
    }

    @FXML
    private void openOutfits() {
        try {
            if (currentUser == null) {
                SceneManager.showAlert("Login Required", "Please login to view your outfits.");
                return;
            }

            if (dataManager.getUserOutfits(currentUser.getUserId()).isEmpty()) {
                SceneManager.showAlert("No Outfits", "You don't have any saved outfits yet.");
                return;
            }

            WindowManager.openOutfitsWindow();
        } catch (Exception e) {
            SceneManager.showErrorAlert("Error", "Failed to open outfits: " + e.getMessage());
        }
    }

    @FXML
    private void openAIStylist() {
        try {
            WindowManager.openAIStylistWindow();
        } catch (Exception e) {
            SceneManager.showAlert("Not Implemented", "AI Stylist feature coming soon!");
        }
    }

    @FXML
    private void handleLogout() {
        dataManager.setCurrentUser(null);
        SceneManager.loadScene("LoginView.fxml");
    }

    @FXML
    private void openCart() {
        WindowManager.openCartWindow();
    }

    @FXML
    public void refreshView() {
        // Reload products
        List<Product> storeItems = dataManager.getVisibleProducts();
        setupFilters(storeItems);
        displayStoreItems(storeItems);
    }

    /**
     * Sets the application to dark mode
     */
    @FXML
    public void setDarkMode() {
        try {
            if (storeItemsPane.getScene() != null) {
                // Remove light theme if present
                storeItemsPane.getScene().getStylesheets().removeIf(
                        style -> style.contains("application.css"));

                // Add dark theme stylesheet
                String darkThemePath = getClass().getResource("/styles/dark-theme.css").toExternalForm();
                if (!storeItemsPane.getScene().getStylesheets().contains(darkThemePath)) {
                    storeItemsPane.getScene().getStylesheets().add(darkThemePath);
                }

                // Apply dark mode to main scene root
                storeItemsPane.getScene().getRoot().getStyleClass().add("dark-mode");
                storeItemsPane.getScene().getRoot().getProperties().put("theme", "dark");
                
                // Explicitly set userLabel to white
                userLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                userLabel.getStyleClass().add("dark-mode");
                
                // Apply consistent filter styling - preserve shape while using dark colors
                styleFiltersForDarkMode();
                
                // Fix for MenuBarButtonStyle error
                fixMenuBarStyling();
                
                // Apply dark mode to all elements in the scene
                applyDarkModeToAllNodes((javafx.scene.Parent) storeItemsPane.getScene().getRoot());
            }
        } catch (Exception e) {
            System.err.println("Failed to apply dark theme: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Style filter controls for dark mode while preserving their original shape
     */
    private void styleFiltersForDarkMode() {
        // Add dark-mode class for CSS styling
        categoryFilter.getStyleClass().add("dark-mode");
        priceFilter.getStyleClass().add("dark-mode");
        searchField.getStyleClass().add("dark-mode");
        
        // Explicitly set userLabel to white with inline style
        userLabel.setStyle("-fx-text-fill: white !important;");
        userLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        
        // Apply specific styling to maintain original shape with dark colors
        String comboStyle = 
                "-fx-background-color: #2a2a2a; " +
                "-fx-text-fill: white !important; " + 
                "-fx-control-inner-background: #2a2a2a; " +
                "-fx-background-radius: 4px; " +
                "-fx-border-radius: 4px; " +
                "-fx-border-color: #3a3a3a; " +
                "-fx-padding: 4px 8px; " +
                "-fx-min-height: 32px; " +
                "-fx-pref-height: 32px;";
        
        categoryFilter.setStyle(comboStyle);
        priceFilter.setStyle(comboStyle);
        searchField.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white !important; -fx-control-inner-background: #2a2a2a;");
        
        // Style the arrow buttons in combo boxes to be more visible
        for (javafx.scene.Node node : categoryFilter.lookupAll(".arrow-button")) {
            node.setStyle("-fx-background-color: transparent;");
        }
        
        for (javafx.scene.Node node : priceFilter.lookupAll(".arrow-button")) {
            node.setStyle("-fx-background-color: transparent;");
        }
        
        // Style the arrows themselves with stronger color
        for (javafx.scene.Node node : categoryFilter.lookupAll(".arrow")) {
            node.setStyle("-fx-background-color: white;");
        }
        
        for (javafx.scene.Node node : priceFilter.lookupAll(".arrow")) {
            node.setStyle("-fx-background-color: white;");
        }
    }
    
    /**
     * Sets the application to light mode
     */
    @FXML
    public void setLightMode() {
        try {
            if (storeItemsPane.getScene() != null) {
                // Remove dark theme if present
                storeItemsPane.getScene().getStylesheets().removeIf(
                        style -> style.contains("dark-theme.css"));

                // Add light theme stylesheet
                String lightThemePath = getClass().getResource("/styles/application.css").toExternalForm();
                if (!storeItemsPane.getScene().getStylesheets().contains(lightThemePath)) {
                    storeItemsPane.getScene().getStylesheets().add(lightThemePath);
                }

                // Remove dark mode classes
                storeItemsPane.getScene().getRoot().getStyleClass().remove("dark-mode");
                categoryFilter.getStyleClass().remove("dark-mode");
                priceFilter.getStyleClass().remove("dark-mode");
                searchField.getStyleClass().remove("dark-mode");
                
                // Reset to default styles
                categoryFilter.setStyle("");
                priceFilter.setStyle("");
                searchField.setStyle("");

                // Store theme preference
                storeItemsPane.getScene().getRoot().getProperties().put("theme", "light");
                
                // Fix for MenuBarButtonStyle error
                fixMenuBarStyling();
                
                // Remove dark mode from all nodes
                removeDarkModeFromAllNodes((javafx.scene.Parent) storeItemsPane.getScene().getRoot());
            }
        } catch (Exception e) {
            System.err.println("Failed to apply light theme: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fixes the MenuBarButtonStyle error by resetting the style of MenuBar and Menu elements
     */
    private void fixMenuBarStyling() {
        try {
            // Find the MenuBar in the scene
            javafx.scene.control.MenuBar menuBar = (javafx.scene.control.MenuBar) storeItemsPane.getScene().lookup(".menu-bar");
            if (menuBar != null) {
                // Reset the style of the MenuBar to prevent binding errors
                menuBar.setStyle(null);
                
                // Reset the style of all Menu buttons
                for (javafx.scene.control.Menu menu : menuBar.getMenus()) {
                    menu.setStyle(null);
                }
                
                // Find and reset any MenuBarButtonStyle elements
                for (javafx.scene.Node node : menuBar.lookupAll(".MenuBarButtonStyle")) {
                    node.setStyle(null);
                }
                
                // Reset any MenuButton styles
                for (javafx.scene.Node node : menuBar.lookupAll(".menu-button")) {
                    node.setStyle(null);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fixing MenuBar styling: " + e.getMessage());
        }
    }

    /**
     * Opens user account settings
     */
    @FXML
    public void openUserAccount() {
        SceneManager.loadScene("UserProfileView.fxml");
    }

    /**
     * Handles exit menu option
     */
    @FXML
    public void handleExit() {
        boolean confirm = SceneManager.showConfirmationDialog(
                "Exit Application",
                "Are you sure you want to exit?",
                "Any unsaved changes will be lost.");

        if (confirm) {
            Stage stage = (Stage) storeItemsPane.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Opens application preferences
     */
    @FXML
    public void openPreferences() {
        SceneManager.showAlert("Preferences", "Application preferences will be implemented in a future update.");
    }

    /**
     * Opens notification settings
     */
    @FXML
    public void openNotificationSettings() {
        SceneManager.showAlert("Notification Settings",
                "Notification settings will be implemented in a future update.");
    }

    /**
     * Opens about dialog
     */
    @FXML
    public void openAbout() {
        SceneManager.showAlert("About Fashion Store",
                "Fashion Store Application\nVersion 1.0\n" +
                        "A virtual fashion store experience allowing users to create and manage outfits.");
    }

    /**
     * Opens help dialog
     */
    @FXML
    public void openHelp() {
        SceneManager.showAlert("Help",
                "Fashion Store Help:\n\n" +
                        "- Browse products in the main store view\n" +
                        "- Add items to your wardrobe\n" +
                        "- Create outfits from your wardrobe items\n" +
                        "- Use the AI Stylist for outfit recommendations\n\n" +
                        "For more help, please refer to the user manual.");
    }

    /**
     * Called when a filter changes to filter the displayed items
     */
    @FXML
    private void filterItems() {
        // This is a direct handler for filter changes
        System.out.println("Direct filter triggered");
        
        // Get the selected values
        String selectedCategory = categoryFilter.getValue();
        String selectedPrice = priceFilter.getValue();
        
        System.out.println("Direct filtering with category: '" + selectedCategory + 
                       "', price: '" + selectedPrice + "'");
        
        // Get ALL products to ensure we filter everything including featured items
        List<Product> allProducts = dataManager.getAllProducts();
        
        // Apply the filters
        List<Product> filteredItems = allProducts.stream()
            .filter(product -> {
                // Category filter
                boolean matchesCategory = "All Categories".equals(selectedCategory) ||
                        product.getCategory().equals(selectedCategory);
                
                // Price filter
                boolean matchesPrice;
                switch (selectedPrice) {
                    case "Under $50":
                        matchesPrice = product.getPrice().compareTo(new BigDecimal("50")) < 0;
                        break;
                    case "$50 - $100":
                        matchesPrice = product.getPrice().compareTo(new BigDecimal("50")) >= 0 && 
                                      product.getPrice().compareTo(new BigDecimal("100")) <= 0;
                        break;
                    case "$100 - $200":
                        matchesPrice = product.getPrice().compareTo(new BigDecimal("100")) > 0 && 
                                      product.getPrice().compareTo(new BigDecimal("200")) <= 0;
                        break;
                    case "Over $200":
                        matchesPrice = product.getPrice().compareTo(new BigDecimal("200")) > 0;
                        break;
                    default: // "All Prices"
                        matchesPrice = true;
                        break;
                }
                
                return matchesCategory && matchesPrice;
            })
            .collect(Collectors.toList());
        
        // Display the filtered items
        System.out.println("Direct filter found " + filteredItems.size() + " items");
        displayStoreItems(filteredItems);
    }

    /**
     * Recursively applies dark mode styling to all nodes in the scene graph
     */
    private void applyDarkModeToAllNodes(javafx.scene.Parent parent) {
        if (parent == null) return;
        
        if (!parent.getStyleClass().contains("dark-mode")) {
            parent.getStyleClass().add("dark-mode");
        }
        
        // Ensure text elements have proper color in dark mode
        if (parent instanceof javafx.scene.control.Labeled) {
            ((javafx.scene.control.Labeled) parent).setTextFill(javafx.scene.paint.Color.WHITE);
            ((javafx.scene.control.Labeled) parent).setStyle("-fx-text-fill: white !important;");
        } else if (parent instanceof javafx.scene.control.TextInputControl) {
            ((javafx.scene.control.TextInputControl) parent).setStyle("-fx-text-fill: white !important; -fx-background-color: #2a2a2a; -fx-control-inner-background: #2a2a2a;");
        } else if (parent instanceof javafx.scene.control.ComboBox) {
            parent.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white !important;");
        } else if (parent instanceof javafx.scene.control.TabPane) {
            // Explicitly style tab pane
            styleTabPaneForDarkMode((javafx.scene.control.TabPane) parent);
        }
        
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (!node.getStyleClass().contains("dark-mode")) {
                node.getStyleClass().add("dark-mode");
            }
            
            // Handle text elements explicitly
            if (node instanceof javafx.scene.control.Labeled) {
                ((javafx.scene.control.Labeled) node).setTextFill(javafx.scene.paint.Color.WHITE);
                ((javafx.scene.control.Labeled) node).setStyle("-fx-text-fill: white !important;");
            } else if (node instanceof javafx.scene.control.TextInputControl) {
                ((javafx.scene.control.TextInputControl) node).setStyle("-fx-text-fill: white !important; -fx-background-color: #2a2a2a; -fx-control-inner-background: #2a2a2a;");
            } else if (node instanceof javafx.scene.control.ComboBox) {
                node.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: white !important;");
            } else if (node instanceof javafx.scene.control.TabPane) {
                // Explicitly style tab pane
                styleTabPaneForDarkMode((javafx.scene.control.TabPane) node);
            }
            
            if (node instanceof javafx.scene.Parent) {
                applyDarkModeToAllNodes((javafx.scene.Parent) node);
            }
        }
    }

    /**
     * Style TabPane specifically for dark mode
     */
    private void styleTabPaneForDarkMode(javafx.scene.control.TabPane tabPane) {
        // Style the tab pane
        tabPane.setStyle("-fx-background-color: #1e1e1e;");
        
        // Find and style the tab header area
        javafx.scene.Node tabHeaderArea = tabPane.lookup(".tab-header-area");
        if (tabHeaderArea != null) {
            tabHeaderArea.setStyle("-fx-background-color: #121212 !important;");
        }
        
        // Find and style the tab header background
        javafx.scene.Node tabHeaderBackground = tabPane.lookup(".tab-header-background");
        if (tabHeaderBackground != null) {
            tabHeaderBackground.setStyle("-fx-background-color: #121212 !important;");
        }
        
        // Style tab labels to ensure they're white
        for (javafx.scene.control.Tab tab : tabPane.getTabs()) {
            javafx.scene.Node tabLabel = tab.getGraphic();
            if (tabLabel instanceof javafx.scene.control.Labeled) {
                ((javafx.scene.control.Labeled) tabLabel).setTextFill(javafx.scene.paint.Color.WHITE);
                ((javafx.scene.control.Labeled) tabLabel).setStyle("-fx-text-fill: white !important;");
            }
        }
    }

    /**
     * Recursively removes dark mode styling from all nodes in the scene graph
     */
    private void removeDarkModeFromAllNodes(javafx.scene.Parent parent) {
        if (parent == null) return;
        
        parent.getStyleClass().remove("dark-mode");
        
        // Restore text color for light mode
        if (parent instanceof javafx.scene.control.Labeled) {
            ((javafx.scene.control.Labeled) parent).setTextFill(javafx.scene.paint.Color.BLACK);
        } else if (parent instanceof javafx.scene.control.TextInputControl) {
            ((javafx.scene.control.TextInputControl) parent).setStyle("");
        } else if (parent instanceof javafx.scene.control.ComboBox) {
            parent.setStyle("");
        }
        
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            node.getStyleClass().remove("dark-mode");
            
            // Handle text elements explicitly
            if (node instanceof javafx.scene.control.Labeled) {
                ((javafx.scene.control.Labeled) node).setTextFill(javafx.scene.paint.Color.BLACK);
            } else if (node instanceof javafx.scene.control.TextInputControl) {
                ((javafx.scene.control.TextInputControl) node).setStyle("");
            } else if (node instanceof javafx.scene.control.ComboBox) {
                node.setStyle("");
            }
            
            if (node instanceof javafx.scene.Parent) {
                removeDarkModeFromAllNodes((javafx.scene.Parent) node);
            }
        }
    }

    /**
     * Toggles between dark and light mode
     */
    @FXML
    public void toggleTheme() {
        try {
            if (storeItemsPane.getScene() != null) {
                String currentTheme = "light"; // Default theme
                if (storeItemsPane.getScene().getRoot().getProperties().get("theme") != null) {
                    currentTheme = (String) storeItemsPane.getScene().getRoot().getProperties().get("theme");
                }
                
                if ("dark".equals(currentTheme)) {
                    // Currently dark, switch to light
                    setLightMode();
                    themeToggleButton.setText("Dark Mode");
                } else {
                    // Currently light, switch to dark
                    setDarkMode();
                    themeToggleButton.setText("Light Mode");
                }
            }
        } catch (Exception e) {
            SceneManager.showErrorAlert("Theme Error", "Failed to toggle theme: " + e.getMessage());
            e.printStackTrace();
        }
    }
}