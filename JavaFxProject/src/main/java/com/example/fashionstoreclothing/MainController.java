package com.example.fashionstoreclothing;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class MainController implements Initializable {

    @FXML
    private Label userLabel;
    @FXML
    private Label dateTimeLabel;
    @FXML
    private TableView<ClothingItem> itemTable;
    @FXML
    private TableColumn<ClothingItem, String> idColumn;
    @FXML
    private TableColumn<ClothingItem, String> nameColumn;
    @FXML
    private TableColumn<ClothingItem, String> categoryColumn;
    @FXML
    private TableColumn<ClothingItem, String> sizeColumn;
    @FXML
    private TableColumn<ClothingItem, String> colorColumn;
    @FXML
    private TableColumn<ClothingItem, Double> priceColumn;
    @FXML
    private TableColumn<ClothingItem, Integer> quantityColumn;

    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ComboBox<String> sizeComboBox;
    @FXML
    private TextField colorField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField quantityField;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearButton;
    @FXML
    private Label statusLabel;

    private Stage stage;
    private Inventory inventory;
    private FileHandler fileHandler;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupComboBoxes();

        fileHandler = new FileHandler();
        inventory = fileHandler.loadInventory();
        if (inventory == null) {
            inventory = new Inventory();
            // Add some sample data
            addSampleData();
        }

        refreshTable();

        // Add listener for table selection
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                clearFields();
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    }

    private void setupComboBoxes() {
        // Setup category options
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Shirts", "Pants", "Dresses", "Skirts", "Jackets", "Shoes", "Accessories"
        );
        categoryComboBox.setItems(categories);

        // Setup size options
        ObservableList<String> sizes = FXCollections.observableArrayList(
                "XS", "S", "M", "L", "XL", "XXL"
        );
        sizeComboBox.setItems(sizes);
    }

    @FXML
    private void handleAddAction(ActionEvent event) {
        if (validateInputs()) {
            ClothingItem newItem = createItemFromFields();
            inventory.addItem(newItem);
            refreshTable();
            clearFields();
            saveData();
            showStatus("Item added successfully!");
        }
    }

    @FXML
    private void handleUpdateAction(ActionEvent event) {
        ClothingItem selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null && validateInputs()) {
            ClothingItem updatedItem = createItemFromFields();
            updatedItem.setId(selectedItem.getId()); // Preserve the original ID
            inventory.updateItem(selectedItem, updatedItem);
            refreshTable();
            clearFields();
            saveData();
            showStatus("Item updated successfully!");
        }
    }

    @FXML
    private void handleDeleteAction(ActionEvent event) {
        ClothingItem selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Item");
            alert.setContentText("Are you sure you want to delete the selected item?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                inventory.removeItem(selectedItem);
                refreshTable();
                clearFields();
                saveData();
                showStatus("Item deleted successfully!");
            }
        }
    }

    @FXML
    private void handleClearAction(ActionEvent event) {
        clearFields();
        itemTable.getSelectionModel().clearSelection();
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errorMessage.append("Name is required.\n");
        }

        if (categoryComboBox.getValue() == null) {
            errorMessage.append("Category is required.\n");
        }

        if (sizeComboBox.getValue() == null) {
            errorMessage.append("Size is required.\n");
        }

        if (colorField.getText().trim().isEmpty()) {
            errorMessage.append("Color is required.\n");
        }

        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price < 0) {
                errorMessage.append("Price must be a positive number.\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Price must be a valid number.\n");
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity < 0) {
                errorMessage.append("Quantity must be a positive integer.\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("Quantity must be a valid integer.\n");
        }

        if (errorMessage.length() > 0) {
            showError("Validation Error", errorMessage.toString());
            return false;
        }

        return true;
    }

    private ClothingItem createItemFromFields() {
        String id = UUID.randomUUID().toString().substring(0, 8);
        String name = nameField.getText().trim();
        String category = categoryComboBox.getValue();
        String size = sizeComboBox.getValue();
        String color = colorField.getText().trim();
        double price = Double.parseDouble(priceField.getText().trim());
        int quantity = Integer.parseInt(quantityField.getText().trim());

        return new ClothingItem(id, name, category, size, color, price, quantity);
    }

    private void populateFields(ClothingItem item) {
        nameField.setText(item.getName());
        categoryComboBox.setValue(item.getCategory());
        sizeComboBox.setValue(item.getSize());
        colorField.setText(item.getColor());
        priceField.setText(String.valueOf(item.getPrice()));
        quantityField.setText(String.valueOf(item.getQuantity()));
    }

    private void clearFields() {
        nameField.clear();
        categoryComboBox.setValue(null);
        sizeComboBox.setValue(null);
        colorField.clear();
        priceField.clear();
        quantityField.clear();
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void refreshTable() {
        itemTable.setItems(inventory.getObservableItems());
    }

    private void saveData() {
        if (fileHandler.saveInventory(inventory)) {
            showStatus("Data saved successfully!");
        } else {
            showError("Save Error", "Failed to save data to file.");
        }
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        // Clear status after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addSampleData() {
        inventory.addItem(new ClothingItem("1", "Blue T-Shirt", "Shirts", "M", "Blue", 19.99, 50));
        inventory.addItem(new ClothingItem("2", "Black Jeans", "Pants", "L", "Black", 39.99, 30));
        inventory.addItem(new ClothingItem("3", "Red Dress", "Dresses", "S", "Red", 49.99, 20));
        inventory.addItem(new ClothingItem("4", "Leather Jacket", "Jackets", "XL", "Brown", 89.99, 15));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        // Set window close event
        stage.setOnCloseRequest(event -> {
            saveData();
        });
    }

    public void updateDateTimeLabel(String dateTime) {
        if (dateTimeLabel != null) {
            dateTimeLabel.setText("Date: " + dateTime);
        }
    }

    public void setUserLabel(String username) {
        if (userLabel != null) {
            userLabel.setText("User: " + username);
        }
    }
}