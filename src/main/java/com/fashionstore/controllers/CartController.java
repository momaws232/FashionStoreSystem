package com.fashionstore.controllers;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.fashionstore.application.FashionStoreApp;
import com.fashionstore.models.ShoppingCart;
import com.fashionstore.storage.DataManager;
import com.fashionstore.utils.SceneManager;
import com.fashionstore.utils.WindowManager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;

public class CartController implements Initializable {

    @FXML
    private TableView<ShoppingCart.CartItem> cartItemsTable;
    @FXML
    private TableColumn<ShoppingCart.CartItem, String> itemColumn;
    @FXML
    private TableColumn<ShoppingCart.CartItem, String> priceColumn;
    @FXML
    private TableColumn<ShoppingCart.CartItem, Integer> quantityColumn;
    @FXML
    private TableColumn<ShoppingCart.CartItem, String> subtotalColumn;
    @FXML
    private TableColumn<ShoppingCart.CartItem, Button> actionsColumn;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private Label emptyCartLabel;
    @FXML
    private Button checkoutButton;
    @FXML
    private Button clearCartButton;
    @FXML
    private Button continueShoppingButton;
    @FXML
    private Button backToShoppingButton;

    private DataManager dataManager;
    private ShoppingCart cart;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("CartController: Initializing cart view");
        
        try {
            // Initialize the data manager
            initializeDataManager();
            System.out.println("CartController: Data manager initialized");
            
            // Load the cart data
            initializeCart();
            System.out.println("CartController: Cart initialized with " + 
                              (cart != null ? cart.getItems().size() : 0) + " items");
            
            // Set up the table columns
            setupTableColumns();
            System.out.println("CartController: Table columns configured");
            
            // Refresh the cart display
            refreshCartDisplay();
            System.out.println("CartController: Initial cart display refreshed");
            
        } catch (Exception e) {
            System.err.println("CartController: Initialization error: " + e.getMessage());
            e.printStackTrace();
            handleInitializationError(e);
        }
    }

    private void initializeDataManager() {
        dataManager = FashionStoreApp.getDataManager();
        if (dataManager == null || dataManager.getCurrentUser() == null) {
            SceneManager.loadScene("LoginView.fxml");
            throw new IllegalStateException("User not logged in");
        }
    }

    private void initializeCart() {
        cart = dataManager.getCart(dataManager.getCurrentUser().getUserId());
        if (cart == null) {
            cart = new ShoppingCart(dataManager.getCurrentUser().getUserId());
            // Save directly without triggering any UI updates
            dataManager.saveCart(cart);
        }
        
        // Don't automatically call refreshCartDisplay or updateCart here
        // Those will be called after setupTableColumns
    }

    private void setupTableColumns() {
        itemColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getName()));

        priceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                currencyFormat.format(cellData.getValue().getProduct().getPrice())));

        quantityColumn.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());

        quantityColumn.setCellFactory(createQuantityCellFactory());

        subtotalColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(currencyFormat.format(cellData.getValue().getTotalPrice())));

        actionsColumn.setCellFactory(createActionsCellFactory());
    }

    private Callback<TableColumn<ShoppingCart.CartItem, Integer>, TableCell<ShoppingCart.CartItem, Integer>> createQuantityCellFactory() {
        return column -> new TableCell<ShoppingCart.CartItem, Integer>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 100, 1);
            private boolean updatingFromModel = false;

            {
                spinner.setEditable(true);
                
                // Configure the spinner value factory to properly handle changes
                SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
                spinner.setValueFactory(valueFactory);
                
                // Add proper change listener with guard against recursive calls
                spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                    if (updatingFromModel || newValue == null || oldValue == null || oldValue.equals(newValue)) {
                        return; // Avoid recursive updates
                    }
                    
                    int index = getIndex();
                    if (index < 0 || index >= getTableView().getItems().size()) {
                        return; // Invalid index
                    }
                    
                    // Get the item from the table
                    ShoppingCart.CartItem item = getTableView().getItems().get(index);
                    if (item != null) {
                        System.out.println("FIXED SPINNER: Changing quantity for " + 
                                            (item.getProduct() != null ? item.getProduct().getName() : "null") + 
                                            " from " + oldValue + " to " + newValue);
                        
                        try {
                            // Set flag to prevent recursive updates
                            updatingFromModel = true;
                            
                            // Update the item quantity
                            item.setQuantity(newValue);
                            
                            // Save the cart state directly without any handlers
                            dataManager.saveCart(cart);
                            
                            // Update the UI manually without calling updateCart
                            totalAmountLabel.setText(currencyFormat.format(cart.getTotalPrice()));
                            
                            // Force refresh THIS cell only to update subtotals
                            getTableView().getColumns().get(3).setVisible(false);
                            getTableView().getColumns().get(3).setVisible(true);
                        } finally {
                            // Ensure flag is reset even if exception occurs
                            updatingFromModel = false;
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                
                // Clear the graphic first
                setGraphic(null);
                
                if (empty || quantity == null) {
                    return;
                }
                
                try {
                    updatingFromModel = true;
                    
                    // Get the current item to access its stock information
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        ShoppingCart.CartItem item = getTableView().getItems().get(index);
                        if (item != null && item.getProduct() != null) {
                            // Set max value to the available stock
                            int stockQuantity = item.getProduct().getStockQuantity();
                            
                            // Update spinner value factory with stock-limited max
                            SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
                                (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory();
                            valueFactory.setMax(stockQuantity);
                            
                            // Set the current quantity value
                            valueFactory.setValue(quantity);
                        }
                    }
                    
                    // Set the spinner as the cell's graphic
                    setGraphic(spinner);
                } finally {
                    updatingFromModel = false;
                }
            }
        };
    }

    private Callback<TableColumn<ShoppingCart.CartItem, Button>, TableCell<ShoppingCart.CartItem, Button>> createActionsCellFactory() {
        return column -> new TableCell<ShoppingCart.CartItem, Button>() {
            // Create a new button for each row
            private final Button button = new Button("Remove");

            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                
                // Always clear the graphic first
                setGraphic(null);
                
                if (empty) {
                    return;
                }
                
                // Style the button
                button.getStyleClass().add("remove-button");
                button.setMaxWidth(Double.MAX_VALUE);
                
                // Set the button action directly here
                button.setOnAction(event -> {
                    // Get the item directly from this cell's table row
                    int idx = getIndex();
                    System.out.println("Remove button clicked at index: " + idx);
                    
                    // Safety check the table view
                    if (getTableView() == null) {
                        System.err.println("TableView is null");
                        return;
                    }
                    
                    // Safety check the table's items
                    if (getTableView().getItems() == null) {
                        System.err.println("Items list is null");
                        return;
                    }
                    
                    // Get the item
                    if (idx >= 0 && idx < getTableView().getItems().size()) {
                        ShoppingCart.CartItem cartItem = getTableView().getItems().get(idx);
                        if (cartItem != null && cartItem.getProduct() != null) {
                            String prodId = cartItem.getProduct().getProductId();
                            String prodName = cartItem.getProduct().getName();
                            int qty = cartItem.getQuantity();
                            
                            System.out.println("Removing item: " + prodName + 
                                              " (ID: " + prodId + ", quantity: " + qty + ")");
                            
                            // Use the dedicated removeFromCart method for more robust handling
                            removeFromCart(prodId);
                        }
                    }
                });
                
                // Add the button to the cell
                setGraphic(button);
            }
        };
    }

    /**
     * Safely refreshes the cart display without causing recursive updates.
     */
    private void refreshCartDisplay() {
        System.out.println("FIXED refreshCartDisplay: Safely updating display");
        
        try {
            // Temporarily disconnect the table from its data source
            cartItemsTable.setItems(null);
            
            // Create a completely disconnected copy of items to prevent callbacks
            List<ShoppingCart.CartItem> items = new ArrayList<>(cart.getItems());
            
            // Set up totally new observable list
            javafx.collections.ObservableList<ShoppingCart.CartItem> observableItems = 
                FXCollections.observableArrayList(items);
            
            // Update UI elements directly
            totalAmountLabel.setText(currencyFormat.format(cart.getTotalPrice()));
            boolean isEmpty = cart.isEmpty();
            emptyCartLabel.setVisible(isEmpty);
            clearCartButton.setDisable(isEmpty);
            checkoutButton.setDisable(isEmpty);
            
            // Now reattach to the table
            cartItemsTable.setItems(observableItems);
            
            // Force one-time refresh without triggering additional updates
            cartItemsTable.refresh();
            
            System.out.println("FIXED refreshCartDisplay: Display updated with " + items.size() + " items");
        } catch (Exception e) {
            System.err.println("FIXED refreshCartDisplay: Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * THIS METHOD IS DISABLED TO PREVENT RECURSIVE UPDATES.
     * DO NOT USE - use direct UI updates instead.
     */
    /*
    private void updateCart() {
        try {
            // Recalculate and display the total price
            totalAmountLabel.setText(currencyFormat.format(cart.getTotalPrice()));
            
            // Check if the cart is empty
            boolean isEmpty = cart.isEmpty();
            
            // Show/hide empty cart message
            emptyCartLabel.setVisible(isEmpty);
            
            // Enable/disable buttons based on cart state
            clearCartButton.setDisable(isEmpty);
            checkoutButton.setDisable(isEmpty);
            
            // Save cart changes to data store
            dataManager.saveCart(cart);
            
            // Log the cart status
            System.out.println("CartController.updateCart: Updated UI for cart with " + 
                              cart.getItems().size() + " items, total: " + 
                              currencyFormat.format(cart.getTotalPrice()));
        } catch (Exception e) {
            System.err.println("CartController.updateCart: Error updating cart UI: " + e.getMessage());
            e.printStackTrace();
        }
    }
    */

    /**
     * Removes an item completely from the cart, regardless of quantity.
     * This implementation prevents recursive updates.
     * 
     * @param productId The ID of the product to remove
     */
    private void removeFromCart(String productId) {
        if (productId == null || productId.isEmpty()) {
            System.err.println("Cannot remove: null or empty product ID");
            return;
        }

        System.out.println("EMERGENCY FIX: Starting direct removal for product ID: " + productId);
        
        try {
            // CRITICAL FIX: First, prevent any UI updates while we modify data by disconnecting
            // the table from its data source completely
            cartItemsTable.setItems(null);
            
            // DIRECT APPROACH: Create a completely new cart 
            ShoppingCart newCart = new ShoppingCart(cart.getUserId());
            newCart.setCartId(cart.getCartId());
            
            // Copy all items except the one to remove
            for (ShoppingCart.CartItem item : cart.getItems()) {
                if (item != null && item.getProduct() != null && 
                    !productId.equals(item.getProduct().getProductId())) {
                    // Keep this item by adding it to the new cart
                    newCart.addItem(item.getProduct(), item.getQuantity());
                }
            }
            
            // Replace the cart
            cart = newCart;
            
            // Save to data store WITHOUT calling updateCart, which could cause more events
            dataManager.saveCart(cart);
            
            // Only now update the UI
            if (cart.isEmpty()) {
                cartItemsTable.setItems(FXCollections.observableArrayList());
                emptyCartLabel.setVisible(true);
                clearCartButton.setDisable(true);
                checkoutButton.setDisable(true);
                totalAmountLabel.setText(currencyFormat.format(cart.getTotalPrice()));
            } else {
                // Create a direct unbound copy to prevent callbacks
                java.util.ArrayList<ShoppingCart.CartItem> itemsCopy = new java.util.ArrayList<>(cart.getItems());
                cartItemsTable.setItems(FXCollections.observableArrayList(itemsCopy));
                emptyCartLabel.setVisible(false);
                clearCartButton.setDisable(false);
                checkoutButton.setDisable(false);
                totalAmountLabel.setText(currencyFormat.format(cart.getTotalPrice()));
            }
            
            // Force UI update
            cartItemsTable.refresh();
            
        } catch (Exception e) {
            System.err.println("EMERGENCY FIX ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void clearCart() {
        if (cart.isEmpty())
            return;

        // Use SceneManager for consistent styling
        SceneManager.showConfirmationAlert("Clear Cart",
                "Are you sure you want to remove all items?",
                () -> {
                    // On confirm, use the direct approach
                    try {
                        // Disconnect UI first
                        cartItemsTable.setItems(null);
                        
                        // Create a new cart to replace the old one
                        ShoppingCart newCart = new ShoppingCart(cart.getUserId());
                        newCart.setCartId(cart.getCartId());
                        cart = newCart;
                        
                        // Save to data store
                        dataManager.saveCart(cart);
                        
                        // Update UI directly without callbacks
                        cartItemsTable.setItems(FXCollections.observableArrayList());
                        totalAmountLabel.setText(currencyFormat.format(BigDecimal.ZERO));
                        emptyCartLabel.setVisible(true);
                        clearCartButton.setDisable(true);
                        checkoutButton.setDisable(true);
                    } catch (Exception e) {
                        System.err.println("Error clearing cart: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                null); // No action on cancel
    }

    @FXML
    private void continueShopping() {
        Button sourceButton = continueShoppingButton;
        if (backToShoppingButton != null) {
            sourceButton = backToShoppingButton;
        } else if (continueShoppingButton != null) {
            sourceButton = continueShoppingButton;
        }

        if (sourceButton != null && sourceButton.getScene() != null) {
            ((Stage) sourceButton.getScene().getWindow()).close();
        }
    }

    @FXML
    private void proceedToCheckout() {
        if (cart.isEmpty()) {
            SceneManager.showAlert("Empty Cart", "Your cart is empty. Add items before checkout.");
            return;
        }
        ((Stage) checkoutButton.getScene().getWindow()).close();
        WindowManager.openCheckoutWindow();
    }

    private void handleInitializationError(Exception e) {
        SceneManager.showErrorAlert("Initialization Error", "Failed to initialize cart: " + e.getMessage());
        e.printStackTrace();
    }

    /**
     * Debug version to diagnose removal issues
     */
    private void diagnoseCarts() {
        System.out.println("\n===== CART DIAGNOSIS =====");
        
        System.out.println("-- cartItemsTable items: " + 
            (cartItemsTable.getItems() != null ? cartItemsTable.getItems().size() : "null"));
        
        if (cartItemsTable.getItems() != null) {
            for (int i = 0; i < cartItemsTable.getItems().size(); i++) {
                ShoppingCart.CartItem item = cartItemsTable.getItems().get(i);
                System.out.println("[" + i + "] " + 
                    (item != null ? item.getProduct().getName() + " (ID: " + 
                    item.getProduct().getProductId() + ", qty: " + item.getQuantity() + ")" : "null"));
            }
        }
        
        System.out.println("-- model cart items: " + cart.getItems().size());
        for (int i = 0; i < cart.getItems().size(); i++) {
            ShoppingCart.CartItem item = cart.getItems().get(i);
            System.out.println("[" + i + "] " + 
                (item != null ? item.getProduct().getName() + " (ID: " + 
                item.getProduct().getProductId() + ", qty: " + item.getQuantity() + ")" : "null"));
        }
        
        System.out.println("===== END DIAGNOSIS =====\n");
    }
}