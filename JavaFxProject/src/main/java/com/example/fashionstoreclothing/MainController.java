package com.example.fashionstoreclothing;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainController implements UserAware {
    @FXML private Label dateTimeLabel;
    @FXML private Label userLoginLabel;
    @FXML private TextField searchField;
    @FXML private TableView<ClothingItem> itemTableView;
    @FXML private TableColumn<ClothingItem, Integer> idColumn;
    @FXML private TableColumn<ClothingItem, String> nameColumn;
    @FXML private TableColumn<ClothingItem, String> categoryColumn;
    @FXML private TableColumn<ClothingItem, String> sizeColumn;
    @FXML private TableColumn<ClothingItem, String> colorColumn;
    @FXML private TableColumn<ClothingItem, Double> priceColumn;
    @FXML private TableColumn<ClothingItem, Integer> quantityColumn;
    @FXML private Label statusLabel;
    @FXML private Label totalValueLabel;

    private UserService userService = UserService.getInstance();
    private User currentUser;
    private ObservableList<ClothingItem> inventoryItems;
    private FilteredList<ClothingItem> filteredItems;
    private Inventory inventory;
    private static final String INVENTORY_FILE = "inventory.dat";

    @FXML
    public void initialize() {
        // Set up the current date and time
        updateDateTime();

        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Load inventory data
        loadInventory();

        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterItems(newValue));

        // Start the timer to update date and time
        startDateTimeUpdater();

        // Update total value
        updateTotalValue();
    }

    private void loadInventory() {
        inventory = new Inventory();
        File file = new File(INVENTORY_FILE);

        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                inventory = (Inventory) ois.readObject();
                inventoryItems = inventory.getObservableItems();
            } catch (Exception e) {
                System.err.println("Error loading inventory: " + e.getMessage());
                e.printStackTrace();
                loadSampleData(); // Load sample data if file read fails
            }
        } else {
            loadSampleData(); // Load sample data if file doesn't exist
        }

        filteredItems = new FilteredList<>(inventoryItems, p -> true);
        itemTableView.setItems(filteredItems);
    }

    private void saveInventory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(INVENTORY_FILE))) {
            oos.writeObject(inventory);
            statusLabel.setText("Inventory saved successfully");
        } catch (Exception e) {
            statusLabel.setText("Error saving inventory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSampleData() {
        inventory = new Inventory();
        inventory.addItem(new ClothingItem(1, "Summer T-Shirt", "T-Shirts", "M", "Blue", 19.99, 15));
        inventory.addItem(new ClothingItem(2, "Denim Jeans", "Pants", "32", "Dark Blue", 49.99, 10));
        inventory.addItem(new ClothingItem(3, "Woolen Sweater", "Sweaters", "L", "Gray", 39.99, 8));
        inventory.addItem(new ClothingItem(4, "Cotton Dress", "Dresses", "S", "Floral", 59.99, 12));
        inventory.addItem(new ClothingItem(5, "Winter Jacket", "Outerwear", "XL", "Black", 89.99, 5));

        inventoryItems = inventory.getObservableItems();
        saveInventory();
    }


    private void updateUserDisplay() {
        if (currentUser != null && userLoginLabel != null) {
            userLoginLabel.setText("User: " + currentUser.getUsername());
        }
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTimeLabel.setText("Current Date and Time: " + now.format(formatter));
    }

    private void startDateTimeUpdater() {
        Thread timeUpdater = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(this::updateDateTime);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        timeUpdater.setDaemon(true);
        timeUpdater.start();
    }

    private void filterItems(String searchText) {
        filteredItems.setPredicate(item -> {
            // If filter text is empty, display all items
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }

            // Compare item properties with filter text
            String lowerCaseFilter = searchText.toLowerCase();

            if (item.getName().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (item.getCategory().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (item.getColor().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (item.getSize().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            return false;
        });

        updateTotalValue();
    }

    private void updateTotalValue() {
        double totalValue = filteredItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        totalValueLabel.setText(String.format("Total Value: $%.2f", totalValue));
    }

    @FXML
    protected void handleAddItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fashionstoreclothing/ItemEditorView.fxml"));
            Scene scene = new Scene(loader.load(), 400, 500);

            ItemEditorController controller = loader.getController();
            controller.setMainController(this);
            controller.setupForNewItem();

            Stage stage = new Stage();
            controller.setStage(stage);
            stage.setTitle("Add New Item");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Update total value after dialog is closed
            updateTotalValue();
        } catch (IOException e) {
            statusLabel.setText("Error opening item editor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleEditItem() {
        ClothingItem selectedItem = itemTableView.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fashionstoreclothing/ItemEditorView.fxml"));
                Scene scene = new Scene(loader.load(), 400, 500);

                ItemEditorController controller = loader.getController();
                controller.setMainController(this);
                controller.setupForEdit(selectedItem);

                Stage stage = new Stage();
                controller.setStage(stage);
                stage.setTitle("Edit Item");
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

                // Update total value after dialog is closed
                updateTotalValue();
            } catch (IOException e) {
                statusLabel.setText("Error opening item editor: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("Please select an item to edit");
        }
    }

    @FXML
    protected void handleDeleteItem() {
        ClothingItem selectedItem = itemTableView.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Delete");
            confirmDialog.setHeaderText("Delete Item");
            confirmDialog.setContentText("Are you sure you want to delete " + selectedItem.getName() + "?");

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                inventory.removeItem(selectedItem);
                saveInventory();
                statusLabel.setText("Item deleted: " + selectedItem.getName());
                updateTotalValue();
            }
        } else {
            statusLabel.setText("Please select an item to delete");
        }
    }

    @FXML
    protected void handleLogout() {
        try {
            // Save inventory before logout
            saveInventory();

            // Clear current user on logout
            ClothingStoreApp.setCurrentUser(null);
            ClothingStoreApp.loadNewScene("LoginView.fxml", "Fashion Boutique - Login", 600, 400);
        } catch (IOException e) {
            statusLabel.setText("Error logging out: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Methods for ItemEditorController
    public void addItem(ClothingItem item) {
        inventory.addItem(item);
        saveInventory();
        statusLabel.setText("Item added: " + item.getName());
    }

    public void updateItem(ClothingItem originalItem, ClothingItem updatedItem) {
        inventory.updateItem(originalItem, updatedItem);
        saveInventory();
        statusLabel.setText("Item updated: " + updatedItem.getName());
    }

    public boolean isIdExists(int id) {
        return inventory.findItemById(id) != null;
    }

    @FXML
    public void handleShowAllItems(ActionEvent actionEvent) {
        filteredItems.setPredicate(item -> true);
        statusLabel.setText("Showing all items");
        updateTotalValue();
    }

    @FXML
    public void handleShowLowStock(ActionEvent actionEvent) {
        filteredItems.setPredicate(ClothingItem::isLowStock);
        statusLabel.setText("Showing low stock items only");
        updateTotalValue();
    }

    @FXML
    public void handleSaveInventory(ActionEvent actionEvent) {
        saveInventory();
    }

    @FXML
    public void handleExit(ActionEvent actionEvent) {
        // Save inventory before exit
        saveInventory();
        javafx.application.Platform.exit();
    }
    @FXML private Menu adminMenu;

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserDisplay();

        // Show admin menu only for admin users
        if (adminMenu != null) {
            adminMenu.setVisible(user != null && user.isAdmin());
        }
    }

    @FXML
    public void handleOpenAdminPanel() {
        if (currentUser != null && currentUser.isAdmin()) {
            try {
                ClothingStoreApp.loadNewScene("AdminView.fxml", "Fashion Boutique - Admin Dashboard", 800, 600);
            } catch (IOException e) {
                statusLabel.setText("Error opening admin panel: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("Admin access required");
        }
    }
    @FXML
    public void handleExit() {
        if (currentUser != null) {
            userService.recordLogout(currentUser.getUsername());
        }
        javafx.application.Platform.exit();
    }

    @FXML
    public void refreshInventory(ActionEvent actionEvent) {
        // Reload inventory data from file
        loadInventory();

        // Clear any search filters
        searchField.clear();
        filteredItems.setPredicate(p -> true);

        // Update the UI
        updateTotalValue();
        itemTableView.refresh();

        // Show status message
        statusLabel.setText("Inventory refreshed successfully");
    }
}
