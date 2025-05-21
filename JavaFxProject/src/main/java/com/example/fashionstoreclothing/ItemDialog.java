package com.example.fashionstoreclothing;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

public class ItemDialog extends Dialog<InventoryItem> {

    public ItemDialog(InventoryItem item) {
        boolean isEditing = (item != null);

        setTitle(isEditing ? "Edit Item" : "Add New Item");
        setHeaderText(isEditing ? "Edit item details" : "Enter new item details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextField categoryField = new TextField();
        TextField sizeField = new TextField();
        TextField colorField = new TextField();
        TextField priceField = new TextField();
        TextField quantityField = new TextField();

        // Pre-fill fields if editing
        if (isEditing) {
            nameField.setText(item.getName());
            categoryField.setText(item.getCategory());
            sizeField.setText(item.getSize());
            colorField.setText(item.getColor());
            priceField.setText(String.valueOf(item.getPrice()));
            quantityField.setText(String.valueOf(item.getQuantity()));
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryField, 1, 1);
        grid.add(new Label("Size:"), 0, 2);
        grid.add(sizeField, 1, 2);
        grid.add(new Label("Color:"), 0, 3);
        grid.add(colorField, 1, 3);
        grid.add(new Label("Price:"), 0, 4);
        grid.add(priceField, 1, 4);
        grid.add(new Label("Quantity:"), 0, 5);
        grid.add(quantityField, 1, 5);

        getDialogPane().setContent(grid);

        // Enable/Disable save button depending on whether a name was entered
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Validate input
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        // Convert the result
        setResultConverter(new Callback<ButtonType, InventoryItem>() {
            @Override
            public InventoryItem call(ButtonType button) {
                if (button == saveButtonType) {
                    try {
                        double price = Double.parseDouble(priceField.getText());
                        int quantity = Integer.parseInt(quantityField.getText());

                        if (isEditing) {
                            // Update existing item
                            item.setName(nameField.getText());
                            item.setCategory(categoryField.getText());
                            item.setSize(sizeField.getText());
                            item.setColor(colorField.getText());
                            item.setPrice(price);
                            item.setQuantity(quantity);
                            return item;
                        } else {
                            // Create new item with a temporary ID that will be assigned properly in the service
                            return new InventoryItem(
                                    0, // Temporary ID
                                    nameField.getText(),
                                    categoryField.getText(),
                                    sizeField.getText(),
                                    colorField.getText(),
                                    price,
                                    quantity
                            );
                        }
                    } catch (NumberFormatException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Invalid Input");
                        alert.setHeaderText("Invalid number format");
                        alert.setContentText("Please enter valid numbers for price and quantity.");
                        alert.showAndWait();
                        return null;
                    }
                }
                return null;
            }
        });
    }
}