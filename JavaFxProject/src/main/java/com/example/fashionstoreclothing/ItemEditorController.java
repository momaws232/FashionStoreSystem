package com.example.fashionstoreclothing;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ItemEditorController {
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField categoryField;
    @FXML private TextField sizeField;
    @FXML private TextField colorField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private Label titleLabel;
    @FXML private Label messageLabel;

    private ClothingItem item;
    private boolean isNewItem;
    private Stage stage;
    private MainController mainController;

    public void initialize() {
        // Input validation for numeric fields
        priceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                priceField.setText(oldVal);
            }
        });

        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                quantityField.setText(oldVal);
            }
        });

        idField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                idField.setText(oldVal);
            }
        });
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setupForNewItem() {
        isNewItem = true;
        titleLabel.setText("Add New Item");
        idField.setEditable(true);
        clearFields();
    }

    public void setupForEdit(ClothingItem item) {
        this.item = item;
        isNewItem = false;
        titleLabel.setText("Edit Item");

        // ID should not be editable when editing
        idField.setEditable(false);

        // Populate fields with item data
        idField.setText(String.valueOf(item.getId()));
        nameField.setText(item.getName());
        categoryField.setText(item.getCategory());
        sizeField.setText(item.getSize());
        colorField.setText(item.getColor());
        priceField.setText(String.format("%.2f", item.getPrice()));
        quantityField.setText(String.valueOf(item.getQuantity()));
    }

    private void clearFields() {
        idField.clear();
        nameField.clear();
        categoryField.clear();
        sizeField.clear();
        colorField.clear();
        priceField.clear();
        quantityField.clear();
        messageLabel.setText("");
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        int id = Integer.parseInt(idField.getText());
        String name = nameField.getText();
        String category = categoryField.getText();
        String size = sizeField.getText();
        String color = colorField.getText();
        double price = Double.parseDouble(priceField.getText());
        int quantity = Integer.parseInt(quantityField.getText());

        ClothingItem newItem = new ClothingItem(id, name, category, size, color, price, quantity);

        if (isNewItem) {
            mainController.addItem(newItem);
        } else {
            mainController.updateItem(item, newItem);
        }

        stage.close();
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (idField.getText().isEmpty()) {
            errors.append("ID is required.\n");
        }

        if (nameField.getText().isEmpty()) {
            errors.append("Name is required.\n");
        }

        if (categoryField.getText().isEmpty()) {
            errors.append("Category is required.\n");
        }

        if (sizeField.getText().isEmpty()) {
            errors.append("Size is required.\n");
        }

        if (colorField.getText().isEmpty()) {
            errors.append("Color is required.\n");
        }

        if (priceField.getText().isEmpty()) {
            errors.append("Price is required.\n");
        }

        if (quantityField.getText().isEmpty()) {
            errors.append("Quantity is required.\n");
        }

        if (errors.length() > 0) {
            messageLabel.setText(errors.toString());
            return false;
        }

        // Check if ID is unique when adding a new item
        if (isNewItem) {
            int id = Integer.parseInt(idField.getText());
            if (mainController.isIdExists(id)) {
                messageLabel.setText("An item with this ID already exists.");
                return false;
            }
        }

        return true;
    }
}