package com.fashionstore.controllers;

import com.fashionstore.application.FashionStoreApp;
import com.fashionstore.models.Product;
import com.fashionstore.models.User;
import com.fashionstore.models.Outfit;
import com.fashionstore.storage.DataManager;
import com.fashionstore.utils.SceneManager;
import com.fashionstore.utils.WindowManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import javafx.print.PrinterJob;

import javafx.scene.control.Separator;
import java.text.SimpleDateFormat;
import java.math.RoundingMode;
import java.util.Date;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import java.util.stream.Collectors;
import java.util.HashMap;
import com.fashionstore.utils.JsonParser;


public class AdminController {

    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, String> categoryColumn;
    @FXML
    private TableColumn<Product, BigDecimal> priceColumn;
    @FXML
    private TableColumn<Product, Integer> stockColumn;
    @FXML
    private TableColumn<Product, Boolean> visibilityColumn;
    @FXML
    private Label statusLabel;
    @FXML
    private Button themeToggleButton;

    private DataManager dataManager;

    public void initialize() {
        dataManager = FashionStoreApp.getDataManager();
        setupTableColumns();
        refreshProductTable();
        
        // Set initial theme toggle button text
        Platform.runLater(() -> {
            if (productTable.getScene() != null) {
                String currentTheme = "light";
                if (productTable.getScene().getRoot().getProperties().get("theme") != null) {
                    currentTheme = (String) productTable.getScene().getRoot().getProperties().get("theme");
                }
                themeToggleButton.setText(currentTheme.equals("dark") ? "Light Mode" : "Dark Mode");
            }
        });
    }

    /**
     * Sets up the table columns with appropriate cell factories
     */
    private void setupTableColumns() {
        // Just to be safe, reload all data
        dataManager.loadAllData();

        // Initialize table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        visibilityColumn.setCellValueFactory(new PropertyValueFactory<>("visible"));

        // Set visibility column to display "Visible" or "Hidden"
        visibilityColumn.setCellFactory(column -> new TableCell<Product, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Visible" : "Hidden");
                }
            }
        });
    }

    @FXML
    private void backToLogin() {
        SceneManager.loadScene("LoginView.fxml");
    }

    @FXML
    private void openItemAdder() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ItemAdder.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Product");

            // Create scene
            Scene scene = new Scene(root);

            // Get current theme from the main window
            String currentTheme = "light"; // Default theme
            if (productTable.getScene() != null &&
                    productTable.getScene().getRoot().getProperties().get("theme") != null) {
                currentTheme = (String) productTable.getScene().getRoot().getProperties().get("theme");
            }

            // Apply appropriate theme CSS
            if ("dark".equals(currentTheme)) {
                URL darkCssUrl = getClass().getResource("/styles/dark-theme.css");
                if (darkCssUrl != null) {
                    scene.getStylesheets().add(darkCssUrl.toExternalForm());
                }

                // Store the theme preference in the new scene
                scene.getRoot().getProperties().put("theme", "dark");
            } else {
                // Default to light theme
                URL cssUrl = getClass().getResource("/styles/application.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }

                // Store the theme preference in the new scene
                scene.getRoot().getProperties().put("theme", "light");
            }

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setUserData(this);
            
            // Set a reasonable size for the dialog window
            stage.setWidth(800);
            stage.setHeight(700);
            stage.setMinWidth(650);
            stage.setMinHeight(600);
            
            // Allow resizing for better user experience
            stage.setResizable(true);
            
            stage.showAndWait(); // Use showAndWait to ensure refresh after dialog closes

            // Refresh data after dialog closes (even without explicit callback)
            refreshProductTable();
        } catch (IOException e) {
            SceneManager.showErrorAlert("Error", "Failed to load item adder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void editSelectedItem() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                System.out.println("Editing product: " + selected.getProductId() + " - " + selected.getName());
                System.out.println("Current image path: " + selected.getImagePath());

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ItemAdder.fxml"));
                Parent root = loader.load();

                ItemAdderController controller = loader.getController();
                controller.loadProductForEditing(selected);

                Stage stage = new Stage();
                stage.setTitle("Edit Product");

                // Create scene
                Scene scene = new Scene(root);

                // Get current theme from the main window
                String currentTheme = "light"; // Default theme
                if (productTable.getScene() != null &&
                        productTable.getScene().getRoot().getProperties().get("theme") != null) {
                    currentTheme = (String) productTable.getScene().getRoot().getProperties().get("theme");
                }

                // Apply appropriate theme CSS
                if ("dark".equals(currentTheme)) {
                    URL darkCssUrl = getClass().getResource("/styles/dark-theme.css");
                    if (darkCssUrl != null) {
                        scene.getStylesheets().add(darkCssUrl.toExternalForm());
                    }

                    // Store the theme preference in the new scene
                    scene.getRoot().getProperties().put("theme", "dark");
                } else {
                    // Default to light theme
                    URL cssUrl = getClass().getResource("/styles/application.css");
                    if (cssUrl != null) {
                        scene.getStylesheets().add(cssUrl.toExternalForm());
                    }

                    // Store the theme preference in the new scene
                    scene.getRoot().getProperties().put("theme", "light");
                }

                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setUserData(this);
                
                // Set a reasonable size for the dialog window
                stage.setWidth(800);
                stage.setHeight(700);
                stage.setMinWidth(650);
                stage.setMinHeight(600);
                
                // Allow resizing for better user experience
                stage.setResizable(true);

                // Show dialog and wait for it to close
                stage.showAndWait();

                // After dialog closes, force refresh
                System.out.println("Dialog closed, refreshing data...");
                dataManager.loadAllData(); // Reload all data from database
                refreshProductTable();

                // Also refresh other views
                WindowManager.refreshHomeView();
            } catch (IOException e) {
                SceneManager.showErrorAlert("Error", "Failed to load editor: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            SceneManager.showAlert("No Selection", "Please select a product to edit.");
        }
    }

    @FXML
    private void removeSelectedItem() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Use SceneManager's confirmation alert for consistent styling
            SceneManager.showConfirmationAlert(
                    "Confirm Deletion",
                    "Are you sure you want to delete " + selected.getName()
                            + "?\n\nThis will also remove it from all user wardrobes, outfits, and shopping carts.",
                    () -> {
                        // On confirm
                        System.out.println("Deleting product: " + selected.getProductId() + " - " + selected.getName());

                        try {
                            // Delete the product
                            dataManager.deleteProduct(selected.getProductId());

                            // Reload all data from database to ensure consistency
                            dataManager.loadAllData();

                            // Refresh views
                            refreshProductTable();
                            WindowManager.refreshHomeView();

                            setStatus("Product \"" + selected.getName() + "\" has been removed from the system.");
                        } catch (Exception e) {
                            SceneManager.showErrorAlert("Error", "Failed to delete product: " + e.getMessage());
                            e.printStackTrace();
                        }
                    },
                    null); // No action on cancel
        } else {
            SceneManager.showAlert("No Selection", "Please select a product to remove.");
        }
    }

    @FXML
    private void toggleProductVisibility() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Toggle visibility
            boolean newVisibility = !selected.isVisible();
            selected.setVisible(newVisibility);

            // Update product in database
            dataManager.updateProduct(selected);
            dataManager.saveAllData();

            // Refresh views
            refreshProductTable();
            WindowManager.refreshHomeView();

            String statusMessage = "Product \"" + selected.getName() + "\" is now " +
                    (newVisibility ? "visible" : "hidden") + " in store";
            setStatus(statusMessage);
        } else {
            SceneManager.showAlert("No Selection", "Please select a product to toggle visibility.");
        }
    }

    @FXML
    public void refreshView() {
        refreshProductTable();
        setStatus("Data refreshed from database");
    }

    @FXML
    public void setLightMode() {
        try {
            if (productTable.getScene() != null) {
                // Remove dark theme if present
                productTable.getScene().getStylesheets().removeIf(
                        style -> style.contains("dark-theme.css"));

                // Add light theme stylesheet
                String lightThemePath = getClass().getResource("/styles/application.css").toExternalForm();
                if (!productTable.getScene().getStylesheets().contains(lightThemePath)) {
                    productTable.getScene().getStylesheets().add(lightThemePath);
                }

                // Store theme preference
                productTable.getScene().getRoot().getProperties().put("theme", "light");
                
                // Fix for MenuBarButtonStyle error
                fixMenuBarStyling();

                setStatus("Light theme applied");
            }
        } catch (Exception e) {
            System.err.println("Failed to apply light theme: " + e.getMessage());
        }
    }

    @FXML
    public void setDarkMode() {
        try {
            if (productTable.getScene() != null) {
                // Remove light theme if present
                productTable.getScene().getStylesheets().removeIf(
                        style -> style.contains("application.css"));

                // Add dark theme stylesheet
                String darkThemePath = getClass().getResource("/styles/dark-theme.css").toExternalForm();
                if (!productTable.getScene().getStylesheets().contains(darkThemePath)) {
                    productTable.getScene().getStylesheets().add(darkThemePath);
                }

                // Store theme preference
                productTable.getScene().getRoot().getProperties().put("theme", "dark");
                
                // Fix for MenuBarButtonStyle error
                fixMenuBarStyling();

                setStatus("Dark theme applied");
            }
        } catch (Exception e) {
            System.err.println("Failed to apply dark theme: " + e.getMessage());
        }
    }
    
    /**
     * Fixes the MenuBarButtonStyle error by resetting the style of MenuBar and Menu elements
     */
    private void fixMenuBarStyling() {
        try {
            // Find the MenuBar in the scene
            javafx.scene.control.MenuBar menuBar = (javafx.scene.control.MenuBar) productTable.getScene().lookup(".menu-bar");
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

    @FXML
    public void openAbout() {
        SceneManager.showAlert("About Fashion Store - Admin",
                "Fashion Store Admin Panel\nVersion 1.0\n" +
                        "Provides administrative functions for managing the store inventory, users, and analytics.");
    }

    @FXML
    public void handleExit() {
        boolean confirm = SceneManager.showConfirmationDialog(
                "Exit Admin Panel",
                "Are you sure you want to exit the admin panel?",
                "You will be returned to the login screen.");

        if (confirm) {
            backToLogin();
        }
    }

    @FXML
    public void exportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Store Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        fileChooser.setInitialFileName("fashion_store_data_" +
                java.time.LocalDate.now().toString() + ".json");

        Stage stage = (Stage) productTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                // Export all store data to JSON
                exportStoreDataToJson(file);

                // Show success message
                setStatus("Store data exported to " + file.getAbsolutePath());
                SceneManager.showAlert("Export Successful",
                        "Store data has been exported to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                SceneManager.showErrorAlert("Export Error",
                        "Failed to export store data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Exports all store data to a JSON file
     * 
     * @param file The file to export to
     * @throws IOException If there's an error writing to the file
     */
    private void exportStoreDataToJson(File file) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n");

        // Export products
        json.append("  \"products\": [\n");
        List<Product> products = dataManager.getAllProducts();
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            json.append("    {\n");
            json.append("      \"productId\": \"").append(escapeJson(product.getProductId())).append("\",\n");
            json.append("      \"name\": \"").append(escapeJson(product.getName())).append("\",\n");
            json.append("      \"category\": \"").append(escapeJson(product.getCategory())).append("\",\n");
            json.append("      \"description\": \"").append(escapeJson(product.getDescription())).append("\",\n");
            json.append("      \"price\": ").append(product.getPrice()).append(",\n");
            json.append("      \"stockQuantity\": ").append(product.getStockQuantity()).append(",\n");
            json.append("      \"visible\": ").append(product.isVisible()).append(",\n");
            json.append("      \"imagePath\": \"").append(escapeJson(product.getImagePath())).append("\"\n");
            json.append("    }").append(i < products.size() - 1 ? ",\n" : "\n");
        }
        json.append("  ],\n");

        // Export users (excluding sensitive data)
        json.append("  \"users\": [\n");
        List<User> users = dataManager.getAllUsers();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            json.append("    {\n");
            json.append("      \"userId\": \"").append(escapeJson(user.getUserId())).append("\",\n");
            json.append("      \"username\": \"").append(escapeJson(user.getUsername())).append("\",\n");
            json.append("      \"email\": \"").append(escapeJson(user.getEmail())).append("\",\n");
            json.append("      \"firstName\": \"").append(escapeJson(user.getFirstName())).append("\",\n");
            json.append("      \"lastName\": \"").append(escapeJson(user.getLastName())).append("\",\n");

            // Include wardrobe items
            json.append("      \"wardrobeItems\": [");
            List<String> wardrobeItems = user.getWardrobeItemIds();
            for (int j = 0; j < wardrobeItems.size(); j++) {
                json.append("\"").append(escapeJson(wardrobeItems.get(j))).append("\"")
                        .append(j < wardrobeItems.size() - 1 ? ", " : "");
            }
            json.append("],\n");

            // Include outfits
            json.append("      \"outfits\": [");
            List<String> outfits = user.getOutfitIds();
            for (int j = 0; j < outfits.size(); j++) {
                json.append("\"").append(escapeJson(outfits.get(j))).append("\"")
                        .append(j < outfits.size() - 1 ? ", " : "");
            }
            json.append("]\n");

            json.append("    }").append(i < users.size() - 1 ? ",\n" : "\n");
        }
        json.append("  ],\n");

        // Export outfits
        json.append("  \"outfits\": [\n");
        List<Outfit> outfits = new ArrayList<>();
        for (User user : users) {
            List<Outfit> userOutfits = dataManager.getUserOutfits(user.getUserId());
            outfits.addAll(userOutfits);
        }

        for (int i = 0; i < outfits.size(); i++) {
            Outfit outfit = outfits.get(i);
            json.append("    {\n");
            json.append("      \"outfitId\": \"").append(escapeJson(outfit.getOutfitId())).append("\",\n");
            json.append("      \"name\": \"").append(escapeJson(outfit.getName())).append("\",\n");
            json.append("      \"createdBy\": \"").append(escapeJson(outfit.getUserId())).append("\",\n");

            if (outfit.getCreatedAt() != null) {
                json.append("      \"dateCreated\": \"").append(outfit.getCreatedAt().toString()).append("\",\n");
            } else {
                json.append("      \"dateCreated\": null,\n");
            }

            // Include product IDs - convert Set to List if needed
            json.append("      \"productIds\": [");
            List<String> productIds = new ArrayList<>(outfit.getProductIds());
            for (int j = 0; j < productIds.size(); j++) {
                json.append("\"").append(escapeJson(productIds.get(j))).append("\"")
                        .append(j < productIds.size() - 1 ? ", " : "");
            }
            json.append("]\n");

            json.append("    }").append(i < outfits.size() - 1 ? ",\n" : "\n");
        }
        json.append("  ]\n");

        json.append("}");

        // Write to file
        try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
            writer.write(json.toString());
        }
    }

    /**
     * Escapes a string for JSON format
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @FXML
    public void importData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Store Data");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        Stage stage = (Stage) productTable.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            // Confirm import as it will overwrite existing data
            boolean confirm = SceneManager.showConfirmationDialog(
                    "Confirm Import",
                    "Importing data will merge with existing data. Continue?",
                    "This operation cannot be undone once started.");

            if (confirm) {
                try {
                    // Create import dialog to show progress
                    Stage importDialog = new Stage();
                    importDialog.setTitle("Importing Data");
                    importDialog.initModality(Modality.APPLICATION_MODAL);
                    importDialog.initOwner(stage);

                    VBox dialogRoot = new VBox(15);
                    dialogRoot.setPadding(new Insets(20));
                    dialogRoot.setAlignment(Pos.CENTER);

                    Label titleLabel = new Label("Importing Store Data");
                    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                    Label statusLabel = new Label("Processing file: " + file.getName());

                    ProgressBar progressBar = new ProgressBar(0);
                    progressBar.setPrefWidth(300);

                    Label detailsLabel = new Label("Please wait...");

                    Button cancelButton = new Button("Cancel");
                    // We'll use this to track if the import was canceled
                    final boolean[] importCanceled = { false };

                    dialogRoot.getChildren().addAll(
                            titleLabel, statusLabel, progressBar, detailsLabel, cancelButton);

                    // Create scene and apply theme
                    Scene scene = new Scene(dialogRoot);
                    if (productTable.getScene().getStylesheets().stream()
                            .anyMatch(s -> s.contains("dark-theme.css"))) {
                        scene.getStylesheets().add(
                                getClass().getResource("/styles/dark-theme.css").toExternalForm());
                    } else {
                        scene.getStylesheets().add(
                                getClass().getResource("/styles/application.css").toExternalForm());
                    }

                    importDialog.setScene(scene);
                    importDialog.setWidth(400);
                    importDialog.setHeight(250);

                    cancelButton.setOnAction(e -> {
                        importCanceled[0] = true;
                        importDialog.close();
                    });

                    // Show the dialog
                    importDialog.show();

                    // Start the import process in a background task
                    Task<ImportResult> importTask = new Task<ImportResult>() {
                        @Override
                        protected ImportResult call() throws Exception {
                            ImportResult result = new ImportResult();
                            String fileExt = file.getName().toLowerCase();

                            if (fileExt.endsWith(".json")) {
                                try {
                                    // Read and parse JSON file
                                    updateProgress(0.1, 1);
                                    updateMessage("Reading JSON file...");

                                    String jsonContent = readFileContent(file);
                                    if (importCanceled[0])
                                        return result;

                                    // Parse JSON
                                    updateProgress(0.2, 1);
                                    updateMessage("Parsing JSON data...");

                                    JsonParser.JsonObject jsonObject = JsonParser.parseObject(jsonContent);

                                    // Validate JSON structure
                                    updateProgress(0.3, 1);
                                    updateMessage("Validating data structure...");

                                    if (!jsonObject.has("products")) {
                                        throw new Exception("Invalid JSON format: 'products' array not found");
                                    }

                                    // Import products
                                    if (jsonObject.has("products") && !importCanceled[0]) {
                                        updateProgress(0.4, 1);
                                        updateMessage("Importing products...");

                                        JsonParser.JsonArray productsArray = jsonObject.getJsonArray("products");
                                        result.productsImported = importProducts(productsArray);
                                    }

                                    // Import users
                                    if (jsonObject.has("users") && !importCanceled[0]) {
                                        updateProgress(0.6, 1);
                                        updateMessage("Importing users...");

                                        JsonParser.JsonArray usersArray = jsonObject.getJsonArray("users");
                                        result.usersImported = importUsers(usersArray);
                                    }

                                    // Import outfits
                                    if (jsonObject.has("outfits") && !importCanceled[0]) {
                                        updateProgress(0.8, 1);
                                        updateMessage("Importing outfits...");

                                        JsonParser.JsonArray outfitsArray = jsonObject.getJsonArray("outfits");
                                        result.outfitsImported = importOutfits(outfitsArray);
                                    }

                                    updateProgress(0.9, 1);
                                    updateMessage("Finalizing import...");

                                    // Save all data to ensure it's persisted
                                    dataManager.saveAllData();

                                    result.success = true;
                                } catch (Exception e) {
                                    result.errorMessage = e.getMessage();
                                    e.printStackTrace();
                                }
                            } else if (fileExt.endsWith(".csv")) {
                                updateProgress(0.1, 1);
                                updateMessage("CSV import not yet implemented");
                                Thread.sleep(500);
                                result.errorMessage = "CSV import not yet implemented";
                            } else {
                                updateMessage("Unsupported file format");
                                result.errorMessage = "Unsupported file format";
                            }

                            updateProgress(1, 1);
                            if (result.success) {
                                updateMessage("Import completed successfully");
                            } else {
                                updateMessage("Import failed: " + result.errorMessage);
                            }
                            return result;
                        }
                    };

                    // Bind properties to UI
                    progressBar.progressProperty().bind(importTask.progressProperty());
                    detailsLabel.textProperty().bind(importTask.messageProperty());

                    importTask.setOnSucceeded(e -> {
                        ImportResult result = importTask.getValue();
                        importDialog.close();

                        if (result.success) {
                            // Reload all data from database to ensure consistency
                            dataManager.loadAllData();

                            // Refresh views
                            refreshProductTable();

                            // Show success message
                            StringBuilder successMsg = new StringBuilder("Import completed successfully:\n");
                            successMsg.append("• ").append(result.productsImported).append(" products imported\n");
                            successMsg.append("• ").append(result.usersImported).append(" users imported\n");
                            successMsg.append("• ").append(result.outfitsImported).append(" outfits imported");

                            setStatus("Data imported successfully from " + file.getName());
                            SceneManager.showAlert("Import Successful", successMsg.toString());
                        } else {
                            if (importCanceled[0]) {
                                setStatus("Import was canceled");
                            } else {
                                setStatus("Import failed: " + result.errorMessage);
                                SceneManager.showErrorAlert("Import Failed",
                                        "Failed to import data: " + result.errorMessage);
                            }
                        }
                    });

                    importTask.setOnFailed(e -> {
                        importDialog.close();
                        Throwable ex = importTask.getException();
                        SceneManager.showErrorAlert("Import Error",
                                "Failed to import store data: " +
                                        (ex != null ? ex.getMessage() : "Unknown error"));
                        if (ex != null)
                            ex.printStackTrace();
                    });

                    // Start the task
                    new Thread(importTask).start();

                } catch (Exception e) {
                    SceneManager.showErrorAlert("Import Error",
                            "Failed to import store data: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Simple class to track import results
     */
    private static class ImportResult {
        boolean success = false;
        String errorMessage = "";
        int productsImported = 0;
        int usersImported = 0;
        int outfitsImported = 0;
    }

    /**
     * Reads the content of a file into a string
     * 
     * @param file The file to read
     * @return The file content as a string
     * @throws IOException If an error occurs reading the file
     */
    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    @FXML
    public void showUserManagement() {
        try {
            // Create a dialog/stage for user management
            Stage userManagementStage = new Stage();
            userManagementStage.setTitle("User Management");
            userManagementStage.initModality(Modality.WINDOW_MODAL);
            userManagementStage.initOwner(productTable.getScene().getWindow());

            // Create a VBox as the root container
            VBox root = new VBox(10);
            root.setPadding(new Insets(20));
            root.setMinWidth(800);
            root.setMinHeight(600);

            // Add a header
            Label headerLabel = new Label("User Management");
            headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            headerLabel.getStyleClass().add("header-title");

            // Create a TableView for users
            TableView<User> userTable = new TableView<>();
            userTable.setPrefHeight(400);
            userTable.setPlaceholder(new Label("No users found"));

            // Create columns
            TableColumn<User, String> idColumn = new TableColumn<>("User ID");
            idColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
            idColumn.setPrefWidth(150);

            TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            usernameColumn.setPrefWidth(150);

            TableColumn<User, String> emailColumn = new TableColumn<>("Email");
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            emailColumn.setPrefWidth(250);

            TableColumn<User, String> lastLoginColumn = new TableColumn<>("Last Login");
            lastLoginColumn.setCellValueFactory(data -> {
                User user = data.getValue();
                return new SimpleStringProperty(
                        user.getLastLogin() != null ? user.getLastLogin().toString() : "Never");
            });
            lastLoginColumn.setPrefWidth(200);

            TableColumn<User, String> statusColumn = new TableColumn<>("Status");
            statusColumn.setCellValueFactory(data -> {
                User user = data.getValue();
                String status = "";

                if (user.isBanned()) {
                    int daysLeft = user.getDaysLeftOnBan();
                    if (daysLeft > 0) {
                        status = "Banned (" + daysLeft + " days left)";
                    } else if (daysLeft == -1) {
                        status = "Permanently Banned";
                    } else {
                        status = "Ban Expired";
                    }
                } else if (user.isDeactivated()) {
                    status = "Deactivated";
                } else {
                    status = "Active";
                }

                return new SimpleStringProperty(status);
            });

            // Add a special cell factory to apply color styling to the status
            statusColumn.setCellFactory(column -> new TableCell<User, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);

                        // Apply style based on status
                        if (item.contains("Banned")) {
                            setStyle("-fx-text-fill: #cc0000;"); // Red for banned
                        } else if (item.equals("Deactivated")) {
                            setStyle("-fx-text-fill: #cc6600;"); // Orange for deactivated
                        } else if (item.equals("Active")) {
                            setStyle("-fx-text-fill: #009900;"); // Green for active
                        } else {
                            setStyle("");
                        }
                    }
                }
            });

            statusColumn.setPrefWidth(150);

            // Add columns to table
            userTable.getColumns().addAll(idColumn, usernameColumn, emailColumn, lastLoginColumn, statusColumn);

            // Load users
            List<User> users = dataManager.getAllUsers();
            userTable.setItems(FXCollections.observableArrayList(users));

            // Create button bar
            HBox buttonBar = new HBox(10);
            buttonBar.setAlignment(Pos.CENTER_RIGHT);

            Button addUserBtn = new Button("Add User");
            addUserBtn.getStyleClass().add("action-button");
            addUserBtn.setOnAction(e -> {
                // Create a dialog for adding a new user
                Stage addUserStage = new Stage();
                addUserStage.setTitle("Add New User");
                addUserStage.initModality(Modality.WINDOW_MODAL);
                addUserStage.initOwner(userManagementStage);

                // Create form layout
                GridPane form = new GridPane();
                form.setHgap(10);
                form.setVgap(10);
                form.setPadding(new Insets(20));

                // Username field
                Label usernameLabel = new Label("Username:");
                TextField usernameField = new TextField();
                usernameField.setPromptText("Enter username");
                form.add(usernameLabel, 0, 0);
                form.add(usernameField, 1, 0);

                // Email field
                Label emailLabel = new Label("Email:");
                TextField emailField = new TextField();
                emailField.setPromptText("Enter email");
                form.add(emailLabel, 0, 1);
                form.add(emailField, 1, 1);

                // Password field
                Label passwordLabel = new Label("Password:");
                PasswordField passwordField = new PasswordField();
                passwordField.setPromptText("Enter password");
                form.add(passwordLabel, 0, 2);
                form.add(passwordField, 1, 2);

                // First name field
                Label firstNameLabel = new Label("First Name:");
                TextField firstNameField = new TextField();
                firstNameField.setPromptText("Enter first name");
                form.add(firstNameLabel, 0, 3);
                form.add(firstNameField, 1, 3);

                // Last name field
                Label lastNameLabel = new Label("Last Name:");
                TextField lastNameField = new TextField();
                lastNameField.setPromptText("Enter last name");
                form.add(lastNameLabel, 0, 4);
                form.add(lastNameField, 1, 4);

                // Error label
                Label errorLabel = new Label();
                errorLabel.setTextFill(javafx.scene.paint.Color.RED);
                form.add(errorLabel, 0, 5, 2, 1);

                // Button bar
                HBox formButtonBar = new HBox(10);
                formButtonBar.setAlignment(Pos.CENTER_RIGHT);

                Button saveBtn = new Button("Save");
                saveBtn.getStyleClass().add("action-button");
                saveBtn.setOnAction(saveEvent -> {
                    // Validate inputs
                    if (usernameField.getText().trim().isEmpty()) {
                        errorLabel.setText("Username is required");
                        return;
                    }

                    if (emailField.getText().trim().isEmpty()) {
                        errorLabel.setText("Email is required");
                        return;
                    }

                    if (passwordField.getText().trim().isEmpty()) {
                        errorLabel.setText("Password is required");
                        return;
                    }

                    // Check if username already exists
                    if (dataManager.getUserByUsername(usernameField.getText().trim()) != null) {
                        errorLabel.setText("Username already exists");
                        return;
                    }

                    try {
                        // Create new user
                        User newUser = new User(
                                usernameField.getText().trim(),
                                emailField.getText().trim(),
                                passwordField.getText().trim() // The User constructor will handle hashing now
                        );

                        // Set additional properties
                        if (!firstNameField.getText().trim().isEmpty()) {
                            newUser.setFirstName(firstNameField.getText().trim());
                        }

                        if (!lastNameField.getText().trim().isEmpty()) {
                            newUser.setLastName(lastNameField.getText().trim());
                        }

                        // Add user to the system
                        dataManager.addUser(newUser);
                        dataManager.saveAllData();

                        // Refresh the user table
                        userTable.setItems(FXCollections.observableArrayList(dataManager.getAllUsers()));

                        // Close the dialog
                        addUserStage.close();

                        // Show success message
                        setStatus("User '" + newUser.getUsername() + "' created successfully");
                    } catch (Exception ex) {
                        errorLabel.setText("Error creating user: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });

                Button cancelBtn = new Button("Cancel");
                cancelBtn.setOnAction(cancelEvent -> addUserStage.close());

                formButtonBar.getChildren().addAll(saveBtn, cancelBtn);
                form.add(formButtonBar, 0, 6, 2, 1);

                // Create scene and show dialog
                Scene scene = new Scene(form);

                // Apply the same theme as the parent window
                if (productTable.getScene().getStylesheets().stream()
                        .anyMatch(s -> s.contains("dark-theme.css"))) {
                    scene.getStylesheets().add(
                            getClass().getResource("/styles/dark-theme.css").toExternalForm());
                } else {
                    scene.getStylesheets().add(
                            getClass().getResource("/styles/application.css").toExternalForm());
                }

                addUserStage.setScene(scene);
                addUserStage.showAndWait();
            });

            Button removeUserBtn = new Button("Remove User");
            removeUserBtn.getStyleClass().add("delete-button");
            removeUserBtn.setOnAction(e -> {
                User selectedUser = userTable.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    // Prevent admin user deletion
                    if (selectedUser.getUsername().equals("admin")) {
                        SceneManager.showAlert("Cannot Delete Admin",
                                "The default admin account cannot be deleted for system security.");
                        return;
                    }

                    boolean confirm = SceneManager.showConfirmationDialog(
                            "Confirm Delete",
                            "Are you sure you want to delete user " + selectedUser.getUsername() + "?",
                            "This will remove all their data including wardrobes, outfits, and shopping cart.");

                    if (confirm) {
                        try {
                            // Create a progress dialog
                            Stage progressStage = new Stage();
                            progressStage.setTitle("Deleting User");
                            progressStage.initModality(Modality.APPLICATION_MODAL);

                            VBox progressBox = new VBox(10);
                            progressBox.setPadding(new Insets(20));
                            progressBox.setAlignment(Pos.CENTER);

                            Label progressLabel = new Label("Deleting user and associated data...");
                            ProgressBar progressBar = new ProgressBar();
                            progressBar.setPrefWidth(300);

                            progressBox.getChildren().addAll(progressLabel, progressBar);

                            Scene progressScene = new Scene(progressBox);
                            progressStage.setScene(progressScene);
                            progressStage.show();

                            // Use a background task for deletion
                            Task<Boolean> deleteTask = new Task<Boolean>() {
                                @Override
                                protected Boolean call() throws Exception {
                                    String userId = selectedUser.getUserId();

                                    // Step 1: Remove all outfits created by this user
                                    updateProgress(0.1, 1.0);
                                    updateMessage("Removing user outfits...");
                                    List<Outfit> userOutfits = dataManager.getUserOutfits(userId);
                                    for (Outfit outfit : userOutfits) {
                                        dataManager.removeOutfit(outfit.getOutfitId());
                                    }

                                    // Step 2: Remove the user from all outfits they might be associated with
                                    updateProgress(0.3, 1.0);
                                    updateMessage("Updating shared outfits...");

                                    // Step 3: Skip wardrobe items since they'll be deleted with the user
                                    updateProgress(0.5, 1.0);
                                    updateMessage("Preparing to delete user account...");

                                    // Step 4: Remove the user's shopping cart if it exists
                                    updateProgress(0.7, 1.0);
                                    updateMessage("Removing shopping cart data...");
                                    // The cart removal would depend on how your cart is stored
                                    // If there's a dedicated method, call it here

                                    // Step 5: Remove the user from the database
                                    updateProgress(0.9, 1.0);
                                    updateMessage("Deleting user account...");
                                    boolean removed = dataManager.removeUser(userId);

                                    // Final step: Save all changes
                                    updateProgress(1.0, 1.0);
                                    updateMessage("Saving changes...");
                                    dataManager.saveAllData();

                                    return removed;
                                }
                            };

                            // Bind progress UI elements
                            progressBar.progressProperty().bind(deleteTask.progressProperty());
                            progressLabel.textProperty().bind(deleteTask.messageProperty());

                            // Handle completion
                            deleteTask.setOnSucceeded(event -> {
                                progressStage.close();
                                Boolean result = deleteTask.getValue();

                                if (result) {
                                    // Refresh the user table
                                    userTable.setItems(FXCollections.observableArrayList(dataManager.getAllUsers()));

                                    // Show success message
                                    setStatus("User '" + selectedUser.getUsername() + "' deleted successfully");
                                } else {
                                    SceneManager.showErrorAlert("Error Deleting User",
                                            "Failed to delete user from database");
                                }
                            });

                            deleteTask.setOnFailed(event -> {
                                progressStage.close();
                                Throwable exception = deleteTask.getException();
                                SceneManager.showErrorAlert("Error Deleting User",
                                        "Failed to delete user: " +
                                                (exception != null ? exception.getMessage() : "Unknown error"));
                                if (exception != null) {
                                    exception.printStackTrace();
                                }
                            });

                            // Start the deletion task
                            new Thread(deleteTask).start();

                        } catch (Exception ex) {
                            SceneManager.showErrorAlert("Error Deleting User",
                                    "Failed to delete user: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                } else {
                    SceneManager.showAlert("No Selection", "Please select a user to remove");
                }
            });

            Button banUserBtn = new Button("Ban User");
            banUserBtn.getStyleClass().add("warning-button");
            banUserBtn.setOnAction(e -> {
                User selectedUser = userTable.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    // Prevent admin user banning
                    if (selectedUser.getUsername().equals("admin")) {
                        SceneManager.showAlert("Cannot Ban Admin",
                                "The default admin account cannot be banned.");
                        return;
                    }

                    // Create a dialog for banning the user
                    Stage banUserStage = new Stage();
                    banUserStage.setTitle("Ban User: " + selectedUser.getUsername());
                    banUserStage.initModality(Modality.WINDOW_MODAL);
                    banUserStage.initOwner(userManagementStage);

                    // Create form layout
                    GridPane form = new GridPane();
                    form.setHgap(10);
                    form.setVgap(10);
                    form.setPadding(new Insets(20));

                    // Reason field
                    Label reasonLabel = new Label("Ban Reason:");
                    TextField reasonField = new TextField();
                    reasonField.setPromptText("Enter reason for ban");
                    form.add(reasonLabel, 0, 0);
                    form.add(reasonField, 1, 0);

                    // Ban type radio buttons
                    Label banTypeLabel = new Label("Ban Type:");
                    form.add(banTypeLabel, 0, 1);

                    ToggleGroup banTypeGroup = new ToggleGroup();
                    RadioButton permanentRadio = new RadioButton("Permanent");
                    permanentRadio.setToggleGroup(banTypeGroup);
                    permanentRadio.setSelected(true);
                    RadioButton temporaryRadio = new RadioButton("Temporary");
                    temporaryRadio.setToggleGroup(banTypeGroup);

                    HBox radioBox = new HBox(10, permanentRadio, temporaryRadio);
                    form.add(radioBox, 1, 1);

                    // Duration field (for temporary bans)
                    Label durationLabel = new Label("Duration (days):");
                    form.add(durationLabel, 0, 2);

                    Spinner<Integer> durationSpinner = new Spinner<>(1, 365, 7);
                    durationSpinner.setEditable(true);
                    durationSpinner.setDisable(true); // Initially disabled (permanent ban)
                    form.add(durationSpinner, 1, 2);

                    // Enable/disable duration spinner based on radio selection
                    temporaryRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        durationSpinner.setDisable(!newVal);
                    });

                    // Error label
                    Label errorLabel = new Label();
                    errorLabel.setTextFill(javafx.scene.paint.Color.RED);
                    errorLabel.setVisible(false);
                    form.add(errorLabel, 0, 3, 2, 1);

                    // Buttons
                    HBox formButtonBar = new HBox(10);
                    formButtonBar.setAlignment(Pos.CENTER_RIGHT);

                    Button confirmBtn = new Button("Confirm Ban");
                    confirmBtn.getStyleClass().add("warning-button");
                    confirmBtn.setOnAction(event -> {
                        // Validate input
                        if (reasonField.getText().trim().isEmpty()) {
                            errorLabel.setText("Please enter a reason for the ban");
                            errorLabel.setVisible(true);
                            return;
                        }

                        boolean success;
                        String reason = reasonField.getText().trim();

                        if (permanentRadio.isSelected()) {
                            // Permanent ban
                            success = dataManager.banUser(selectedUser.getUserId(), reason);
                        } else {
                            // Temporary ban
                            int days = durationSpinner.getValue();
                            success = dataManager.banUserTemporarily(selectedUser.getUserId(), reason, days);
                        }

                        if (success) {
                            // Reload all users to get fresh user data with updated ban status
                            List<User> updatedUsers = dataManager.getAllUsers();

                            // Refresh the user table
                            userTable.setItems(FXCollections.observableArrayList(updatedUsers));
                            userTable.refresh(); // Explicitly refresh the table view
                            banUserStage.close();

                            // Show success message
                            setStatus("User '" + selectedUser.getUsername() + "' has been banned");
                        } else {
                            errorLabel.setText("Failed to ban user");
                            errorLabel.setVisible(true);
                        }
                    });

                    Button cancelBtn = new Button("Cancel");
                    cancelBtn.setOnAction(event -> banUserStage.close());

                    formButtonBar.getChildren().addAll(confirmBtn, cancelBtn);
                    form.add(formButtonBar, 0, 4, 2, 1);

                    // Create scene and show dialog
                    Scene scene = new Scene(form);

                    // Apply the same theme as the parent window
                    if (productTable.getScene().getStylesheets().stream()
                            .anyMatch(s -> s.contains("dark-theme.css"))) {
                        scene.getStylesheets().add(
                                getClass().getResource("/styles/dark-theme.css").toExternalForm());
                    } else {
                        scene.getStylesheets().add(
                                getClass().getResource("/styles/application.css").toExternalForm());
                    }

                    banUserStage.setScene(scene);
                    banUserStage.showAndWait();
                } else {
                    SceneManager.showAlert("No Selection", "Please select a user to ban");
                }
            });

            Button unbanUserBtn = new Button("Unban User");
            unbanUserBtn.getStyleClass().addAll("action-button", "unban-button");
            // Initially disable the unban button (will be enabled when a banned user is selected)
            unbanUserBtn.setDisable(true);
            unbanUserBtn.setOnAction(e -> {
                User selectedUser = userTable.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    if (!selectedUser.isBanned()) {
                        SceneManager.showAlert("Not Banned", "This user is not currently banned.");
                        return;
                    }

                    boolean confirm = SceneManager.showConfirmationDialog(
                            "Confirm Unban",
                            "Are you sure you want to unban user " + selectedUser.getUsername() + "?",
                            "This will restore their account access immediately.");

                    if (confirm) {
                        boolean success = dataManager.unbanUser(selectedUser.getUserId());
                        if (success) {
                            // Reload all users to get fresh user data with updated ban status
                            List<User> updatedUsers = dataManager.getAllUsers();

                            // Refresh the user table
                            userTable.setItems(FXCollections.observableArrayList(updatedUsers));
                            userTable.refresh(); // Explicitly refresh the table view

                            // Disable the unban button after successful unban
                            unbanUserBtn.setDisable(true);

                            // Show success message
                            setStatus("User '" + selectedUser.getUsername() + "' has been unbanned");
                        } else {
                            SceneManager.showErrorAlert("Error", "Failed to unban user");
                        }
                    }
                } else {
                    SceneManager.showAlert("No Selection", "Please select a user to unban");
                }
            });

            Button closeBtn = new Button("Close");
            closeBtn.setOnAction(e -> userManagementStage.close());

            // Add selection listener to enable/disable unban button based on selected user's banned status
            userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    // Enable unban button only if the user is banned
                    unbanUserBtn.setDisable(!newSelection.isBanned());
                } else {
                    // Disable if no user is selected
                    unbanUserBtn.setDisable(true);
                }
            });

            buttonBar.getChildren().addAll(addUserBtn, banUserBtn, unbanUserBtn, closeBtn);

            // Add all elements to root
            root.getChildren().addAll(headerLabel, userTable, buttonBar);

            // Create the scene and apply styles
            Scene scene = new Scene(root);

            // Apply the same theme as the parent window
            if (productTable.getScene().getStylesheets().stream()
                    .anyMatch(s -> s.contains("dark-theme.css"))) {
                scene.getStylesheets().add(
                        getClass().getResource("/styles/dark-theme.css").toExternalForm());
            } else {
                scene.getStylesheets().add(
                        getClass().getResource("/styles/application.css").toExternalForm());
            }

            userManagementStage.setScene(scene);
            userManagementStage.show();

        } catch (Exception e) {
            SceneManager.showErrorAlert("Error", "Failed to open User Management: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void showAnalytics() {
        try {
            // Create a dialog/stage for analytics
            Stage analyticsStage = new Stage();
            analyticsStage.setTitle("Store Analytics Dashboard");
            analyticsStage.initModality(Modality.WINDOW_MODAL);
            analyticsStage.initOwner(productTable.getScene().getWindow());

            // Create a BorderPane as the root container
            BorderPane root = new BorderPane();
            root.setPadding(new Insets(20));

            // Set consistent window size
            root.setPrefWidth(1024);
            root.setPrefHeight(768);
            root.setMinWidth(960);
            root.setMinHeight(720);

            // Add a header to the top with improved styling
            Label headerLabel = new Label("Store Analytics Dashboard");
            headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            headerLabel.getStyleClass().add("header-title");

            // Add refresh button to header
            Button refreshBtn = new Button("Refresh Data");
            refreshBtn.getStyleClass().add("action-button");
            refreshBtn.setOnAction(e -> refreshAnalyticsData(root));

            HBox headerBox = new HBox(20, headerLabel, refreshBtn);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.setPadding(new Insets(0, 0, 20, 0));
            root.setTop(headerBox);

            // Create tabs for different analytics with improved styling
            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            tabPane.getStyleClass().add("analytics-tabs");

            // Add the inventory analytics tab
            tabPane.getTabs().add(createInventoryTab());

            // Add the sales projection tab
            tabPane.getTabs().add(createSalesProjectionTab());

            // Add the user analytics tab
            tabPane.getTabs().add(createUserAnalyticsTab());

            // Add the outfit trends tab
            tabPane.getTabs().add(createOutfitTrendsTab());

            root.setCenter(tabPane);

            // Add a bottom button bar with improved layout and more options
            HBox buttonBar = new HBox(15);
            buttonBar.setAlignment(Pos.CENTER_RIGHT);
            buttonBar.setPadding(new Insets(20, 0, 0, 0));

            Button exportBtn = new Button("Export Report");
            exportBtn.getStyleClass().add("action-button");
            exportBtn.setOnAction(e -> {
                try {
                    // Configure file chooser for CSV export
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Export Analytics Report");
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
                    fileChooser.setInitialFileName("fashion_store_analytics_" +
                            java.time.LocalDate.now().toString() + ".csv");

                    // Show save dialog
                    Stage stage = (Stage) analyticsStage.getScene().getWindow();
                    File file = fileChooser.showSaveDialog(stage);

                    if (file != null) {
                        // Generate the report
                        generateAnalyticsReport(file);

                        // Show success message
                        setStatus("Analytics report exported to " + file.getAbsolutePath());
                        SceneManager.showAlert("Export Successful",
                                "Analytics report has been exported to:\n" + file.getAbsolutePath());
                    }
                } catch (Exception ex) {
                    SceneManager.showErrorAlert("Export Error",
                            "Failed to export analytics report: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            // Add print report button
            Button printBtn = new Button("Print Report");
            printBtn.getStyleClass().add("secondary-button");
            printBtn.setOnAction(e -> {
                try {
                    // Create and show print report dialog
                    createAndShowPrintReportDialog(analyticsStage);
                } catch (Exception ex) {
                    SceneManager.showErrorAlert("Print Error", "Failed to generate print report: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            Button closeBtn = new Button("Close");
            closeBtn.setOnAction(e -> analyticsStage.close());

            buttonBar.getChildren().addAll(printBtn, exportBtn, closeBtn);
            root.setBottom(buttonBar);

            // Create the scene and apply styles
            Scene scene = new Scene(root);

            // Apply the same theme as the parent window
            if (productTable.getScene().getStylesheets().stream()
                    .anyMatch(s -> s.contains("dark-theme.css"))) {
                scene.getStylesheets().add(
                        getClass().getResource("/styles/dark-theme.css").toExternalForm());

                // Add analytics-specific dark styling
                URL analyticsCssUrl = getClass().getResource("/styles/analytics-dark.css");
                if (analyticsCssUrl != null) {
                    scene.getStylesheets().add(analyticsCssUrl.toExternalForm());
                }
            } else {
                scene.getStylesheets().add(
                        getClass().getResource("/styles/application.css").toExternalForm());

                // Add analytics-specific light styling
                URL analyticsCssUrl = getClass().getResource("/styles/analytics-light.css");
                if (analyticsCssUrl != null) {
                    scene.getStylesheets().add(analyticsCssUrl.toExternalForm());
                }
            }

            analyticsStage.setScene(scene);

            // Set fixed stage size
            analyticsStage.setWidth(1024);
            analyticsStage.setHeight(768);
            analyticsStage.setMinWidth(960);
            analyticsStage.setMinHeight(720);

            // Center on screen
            analyticsStage.centerOnScreen();

            analyticsStage.show();

        } catch (Exception e) {
            SceneManager.showErrorAlert("Error", "Failed to open Analytics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Refreshes all analytics data and updates the UI
     */
    private void refreshAnalyticsData(BorderPane root) {
        try {
            TabPane tabPane = (TabPane) root.getCenter();
            if (tabPane != null) {
                // Get current tab index to restore it after refresh
                int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();

                // Create new tabs
                TabPane newTabPane = new TabPane();
                newTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
                newTabPane.getStyleClass().add("analytics-tabs");

                // Add refreshed tabs
                newTabPane.getTabs().add(createInventoryTab());
                newTabPane.getTabs().add(createSalesProjectionTab());
                newTabPane.getTabs().add(createUserAnalyticsTab());
                newTabPane.getTabs().add(createOutfitTrendsTab());

                // Replace the tab pane and restore selection
                root.setCenter(newTabPane);
                newTabPane.getSelectionModel().select(selectedIndex);

                // Show confirmation
                setStatus("Analytics data refreshed");
            }
        } catch (Exception e) {
            SceneManager.showErrorAlert("Error", "Failed to refresh analytics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Imports analytics data from a file
     */
    private void importAnalyticsData(Stage parentStage) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Analytics Data");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                    new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));

            File file = fileChooser.showOpenDialog(parentStage);

            if (file != null) {
                // Show a confirmation dialog
                boolean confirm = SceneManager.showConfirmationDialog(
                        "Import Analytics Data",
                        "This will import analytics data from the selected file.",
                        "Do you want to continue?");

                if (confirm) {
                    // For now, show a message that this is a future feature
                    SceneManager.showAlert("Import Analytics Data",
                            "Import of analytics data will be implemented in a future version.\n\n" +
                                    "Selected file: " + file.getAbsolutePath());

                    setStatus("Analytics import functionality is planned for a future update");
                }
            }
        } catch (Exception e) {
            SceneManager.showErrorAlert("Import Error",
                    "Failed to import analytics data: " + e.getMessage());
        }
    }

    /**
     * Creates the inventory analytics tab with charts and stats
     */
    private Tab createInventoryTab() {
        Tab tab = new Tab("Inventory");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Get inventory data
        List<Product> products = dataManager.getAllProducts();
        Map<String, Object> metrics = com.fashionstore.utils.AnalyticsService.getInventoryMetrics(products);
        Map<String, Integer> categoryData = com.fashionstore.utils.AnalyticsService.getCategoryDistribution(products);
        Map<String, Integer> stockLevelData = com.fashionstore.utils.AnalyticsService
                .getStockLevelDistribution(products);
        Map<String, Integer> priceRangeData = com.fashionstore.utils.AnalyticsService
                .getPriceRangeDistribution(products);

        // Create metrics grid
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(50);
        statsGrid.setVgap(15);
        statsGrid.setPadding(new Insets(20));
        statsGrid.setStyle("-fx-background-color: rgba(50, 50, 50, 0.1); -fx-background-radius: 5;");

        // Add metrics
        addStatisticToGrid(statsGrid, 0, "Total Products:",
                String.valueOf(metrics.get("totalProducts")));
        addStatisticToGrid(statsGrid, 1, "Total Stock:",
                String.valueOf(metrics.get("totalStock")));
        addStatisticToGrid(statsGrid, 2, "Average Price:",
                String.format("$%.2f", metrics.get("averagePrice")));
        addStatisticToGrid(statsGrid, 3, "Inventory Value:",
                String.format("$%.2f", metrics.get("totalValue")));
        addStatisticToGrid(statsGrid, 4, "Low Stock Items:",
                String.valueOf(metrics.get("lowStockCount")));

        // Add metrics to content
        content.getChildren().add(statsGrid);

        // Create category distribution table
        Label categoryChartTitle = new Label("Category Distribution");
        categoryChartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Map.Entry<String, Integer>> categoryTable = createDataTable(categoryData, "Category", "Count");
        categoryTable.setMinHeight(300);
        categoryTable.setMaxHeight(300);

        VBox categorySection = new VBox(10, categoryChartTitle, categoryTable);
        content.getChildren().add(categorySection);

        // Create stock level table
        Label stockChartTitle = new Label("Stock Level Distribution");
        stockChartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Map.Entry<String, Integer>> stockTable = createDataTable(stockLevelData, "Stock Level", "Count");
        stockTable.setMinHeight(300);
        stockTable.setMaxHeight(300);

        VBox stockSection = new VBox(10, stockChartTitle, stockTable);
        content.getChildren().add(stockSection);

        // Create price range chart
        Label priceChartTitle = new Label("Price Range Distribution");
        priceChartTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        BarChart<String, Number> priceChart = createBarChart(
                priceRangeData,
                "Price Range",
                "Number of Products");
        priceChart.setMinHeight(300);
        priceChart.setMaxHeight(300);

        VBox priceSection = new VBox(10, priceChartTitle, priceChart);
        content.getChildren().add(priceSection);

        scrollPane.setContent(content);
        tab.setContent(scrollPane);
        return tab;
    }

    /**
     * Creates a data table from the given data
     */
    private <K, V> TableView<Map.Entry<K, V>> createDataTable(Map<K, V> data, String keyColumnName,
            String valueColumnName) {
        TableView<Map.Entry<K, V>> table = new TableView<>();

        // Create columns
        TableColumn<Map.Entry<K, V>, String> keyColumn = new TableColumn<>(keyColumnName);
        keyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey().toString()));
        keyColumn.setPrefWidth(200);

        TableColumn<Map.Entry<K, V>, String> valueColumn = new TableColumn<>(valueColumnName);
        valueColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue().toString()));
        valueColumn.setPrefWidth(100);

        table.getColumns().addAll(keyColumn, valueColumn);

        // Add data
        ObservableList<Map.Entry<K, V>> items = FXCollections.observableArrayList(data.entrySet());
        table.setItems(items);

        return table;
    }

    /**
     * Creates the sales projection tab with charts
     */
    private Tab createSalesProjectionTab() {
        Tab tab = new Tab("Sales Forecast");
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Get projection data
        Map<String, Double> projectionData = com.fashionstore.utils.AnalyticsService.getRevenueProjection();

        // Create chart title
        Label projectionTitle = new Label("Revenue Forecast (Next 6 Months)");
        projectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Create line chart for projections
        LineChart<String, Number> projectionChart = createLineChart(
                projectionData,
                "Month",
                "Projected Revenue ($)");
        projectionChart.setMinHeight(400);
        projectionChart.setTitle("Monthly Revenue Projection");

        content.getChildren().addAll(projectionTitle, projectionChart);

        // Add recommendations section
        Label recTitle = new Label("Inventory Recommendations");
        recTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        recTitle.setPadding(new Insets(20, 0, 10, 0));

        VBox recommendationsBox = new VBox(10);
        recommendationsBox.setPadding(new Insets(15));
        recommendationsBox.setStyle("-fx-background-color: rgba(50, 50, 50, 0.1); -fx-background-radius: 5;");

        List<Product> lowStockItems = dataManager.getAllProducts().stream()
                .filter(p -> p.getStockQuantity() < 5 && p.getStockQuantity() > 0)
                .limit(5)
                .collect(Collectors.toList());

        Label recLabel = new Label("Based on current inventory levels and projected sales, consider restocking:");
        recLabel.setWrapText(true);

        recommendationsBox.getChildren().add(recLabel);

        if (lowStockItems.isEmpty()) {
            recommendationsBox.getChildren().add(new Label("• No items currently need restocking"));
        } else {
            for (Product product : lowStockItems) {
                Label itemLabel = new Label("• " + product.getName() + " (Current Stock: " +
                        product.getStockQuantity() + ")");
                recommendationsBox.getChildren().add(itemLabel);
            }
        }

        content.getChildren().addAll(recTitle, recommendationsBox);

        tab.setContent(content);
        return tab;
    }

    /**
     * Creates the user analytics tab with charts
     */
    private Tab createUserAnalyticsTab() {
        Tab tab = new Tab("User Analytics");
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Get user data
        List<User> users = dataManager.getAllUsers();
        Map<java.time.LocalDate, Integer> registrationTrend = com.fashionstore.utils.AnalyticsService
                .getUserRegistrationTrend(users, 30);

        // Create user statistics section
        Label userStatsTitle = new Label("User Statistics");
        userStatsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane userStatsGrid = new GridPane();
        userStatsGrid.setHgap(50);
        userStatsGrid.setVgap(15);
        userStatsGrid.setPadding(new Insets(20));
        userStatsGrid.setStyle("-fx-background-color: rgba(50, 50, 50, 0.1); -fx-background-radius: 5;");

        addStatisticToGrid(userStatsGrid, 0, "Total Users:", String.valueOf(users.size()));

        // Count recent users (last 30 days)
        long recentUsers = users.stream()
                .filter(u -> u.getLastLogin() != null)
                .filter(u -> {
                    Date thirtyDaysAgo = new Date(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L);
                    return u.getLastLogin().after(thirtyDaysAgo);
                })
                .count();

        addStatisticToGrid(userStatsGrid, 1, "Active Users (Last 30 Days):", String.valueOf(recentUsers));

        // Calculate average wardrobe size
        double avgWardrobeSize = users.stream()
                .mapToDouble(u -> u.getWardrobeItemIds().size())
                .average()
                .orElse(0.0);

        addStatisticToGrid(userStatsGrid, 2, "Average Wardrobe Size:",
                String.format("%.1f items", avgWardrobeSize));

        content.getChildren().addAll(userStatsTitle, userStatsGrid);

        // Create registration trend chart
        Label trendTitle = new Label("User Registration Trend (Last 30 Days)");
        trendTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        trendTitle.setPadding(new Insets(20, 0, 10, 0));

        // We need to convert LocalDate to String for the chart
        Map<String, Number> chartData = new LinkedHashMap<>();
        registrationTrend
                .forEach((date, count) -> chartData.put(date.getMonthValue() + "/" + date.getDayOfMonth(), count));

        BarChart<String, Number> registrationChart = createBarChart(
                chartData,
                "Date",
                "New Users");
        registrationChart.setMinHeight(400);
        registrationChart.setCategoryGap(0);

        content.getChildren().addAll(trendTitle, registrationChart);

        tab.setContent(content);
        return tab;
    }

    /**
     * Creates the outfit trends tab with charts and trending outfits
     */
    private Tab createOutfitTrendsTab() {
        Tab tab = new Tab("Outfit Trends");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Get outfit data - collect all users' outfits
        List<Outfit> outfits = new ArrayList<>();
        for (User user : dataManager.getAllUsers()) {
            for (String outfitId : user.getOutfitIds()) {
                Outfit outfit = dataManager.getOutfit(outfitId);
                if (outfit != null) {
                    outfits.add(outfit);
                }
            }
        }

        // Create outfit statistics section
        Label outfitStatsTitle = new Label("Outfit Statistics");
        outfitStatsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane outfitStatsGrid = new GridPane();
        outfitStatsGrid.setHgap(50);
        outfitStatsGrid.setVgap(15);
        outfitStatsGrid.setPadding(new Insets(20));
        outfitStatsGrid.setStyle("-fx-background-color: rgba(50, 50, 50, 0.1); -fx-background-radius: 5;");

        addStatisticToGrid(outfitStatsGrid, 0, "Total Outfits:", String.valueOf(outfits.size()));

        // Count AI-generated outfits
        long aiOutfits = outfits.stream()
                .filter(Outfit::isAiGenerated)
                .count();

        // Avoid division by zero
        double aiPercentage = outfits.isEmpty() ? 0 : (double) aiOutfits / outfits.size() * 100;

        addStatisticToGrid(outfitStatsGrid, 1, "AI-Generated Outfits:",
                String.valueOf(aiOutfits) + " (" + String.format("%.1f%%", aiPercentage) + ")");

        // Calculate average items per outfit
        double avgItems = outfits.stream()
                .mapToDouble(o -> o.getProductIds().size())
                .average()
                .orElse(0.0);

        addStatisticToGrid(outfitStatsGrid, 2, "Average Items per Outfit:",
                String.format("%.1f items", avgItems));

        content.getChildren().addAll(outfitStatsTitle, outfitStatsGrid);

        // Create trending outfits section
        Label trendingTitle = new Label("Top Trending Outfits");
        trendingTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        trendingTitle.setPadding(new Insets(20, 0, 10, 0));

        // Get trending outfits using our service
        List<Map.Entry<Outfit, Integer>> trendingOutfits = com.fashionstore.utils.AnalyticsService
                .getTopTrendingOutfits(outfits, 5);

        VBox trendingOutfitsBox = new VBox(10);
        trendingOutfitsBox.setPadding(new Insets(15));
        trendingOutfitsBox.setStyle("-fx-background-color: rgba(50, 50, 50, 0.1); -fx-background-radius: 5;");

        if (trendingOutfits.isEmpty()) {
            trendingOutfitsBox.getChildren().add(new Label("No outfits available to analyze"));
        } else {
            for (Map.Entry<Outfit, Integer> entry : trendingOutfits) {
                Outfit outfit = entry.getKey();
                Integer score = entry.getValue();

                HBox outfitRow = new HBox(15);
                outfitRow.setAlignment(Pos.CENTER_LEFT);

                Label nameLabel = new Label(outfit.getName());
                nameLabel.setStyle("-fx-font-weight: bold;");

                Label itemsLabel = new Label(outfit.getProductIds().size() + " items");

                Label occasionLabel = new Label("Occasion: " + outfit.getOccasion());

                Label scoreLabel = new Label("Trend Score: " + score);
                scoreLabel.setStyle("-fx-text-fill: #2a5885;");

                outfitRow.getChildren().addAll(nameLabel, itemsLabel, occasionLabel, scoreLabel);
                trendingOutfitsBox.getChildren().add(outfitRow);
            }
        }

        content.getChildren().addAll(trendingTitle, trendingOutfitsBox);

        // Create season popularity table
        Label seasonTitle = new Label("Outfit Season Popularity");
        seasonTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        seasonTitle.setPadding(new Insets(20, 0, 10, 0));

        // Count outfits by season
        Map<String, Integer> seasonData = new LinkedHashMap<>();
        for (Outfit.OutfitSeason season : Outfit.OutfitSeason.values()) {
            seasonData.put(season.name(), 0);
        }

        for (Outfit outfit : outfits) {
            String season = outfit.getSeason().name();
            seasonData.put(season, seasonData.getOrDefault(season, 0) + 1);
        }

        TableView<Map.Entry<String, Integer>> seasonTable = createDataTable(seasonData, "Season", "Count");
        seasonTable.setMinHeight(300);
        seasonTable.setMaxHeight(300);

        content.getChildren().addAll(seasonTitle, seasonTable);

        scrollPane.setContent(content);
        tab.setContent(scrollPane);
        return tab;
    }

    /**
     * Creates a pie chart from the given data
     */
    private <K, V extends Number> TableView<Map.Entry<K, V>> createPieChart(Map<K, V> data, String title) {
        // Replace with table view since PieChart is not available
        return createDataTable(data, "Category", "Value");
    }

    /**
     * Creates a bar chart from the given data
     */
    private BarChart<String, Number> createBarChart(
            Map<String, ? extends Number> data,
            String xAxisLabel,
            String yAxisLabel) {

        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        // Create the chart
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setAnimated(false);

        // Create a data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Add data to series
        for (Map.Entry<String, ? extends Number> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().add(series);
        return chart;
    }

    /**
     * Creates a line chart from the given data
     */
    private LineChart<String, Number> createLineChart(
            Map<String, ? extends Number> data,
            String xAxisLabel,
            String yAxisLabel) {

        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xAxisLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        // Create the chart
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);

        // Create a data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Projected Revenue");

        // Add data to series
        for (Map.Entry<String, ? extends Number> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().add(series);
        return chart;
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    public void saveProduct(Product product) {
        if (product.getProductId() != null) {
            // If product has an ID, use updateProduct
            System.out.println("Saving updated product: " + product.getProductId());
            dataManager.updateProduct(product);
            setStatus("Product \"" + product.getName() + "\" updated successfully");
        } else {
            // If new product, use addProduct
            System.out.println("Saving new product");
            dataManager.addProduct(product);
            setStatus("New product \"" + product.getName() + "\" added successfully");
        }
        dataManager.saveAllData(); // Explicit save after addition/modification
        refreshProductTable();

        // Also refresh store views
        WindowManager.refreshHomeView();
    }

    public void refreshProductTable() {
        try {
            // Get fresh data from the data manager
            ObservableList<Product> products = FXCollections.observableArrayList(dataManager.getAllProducts());

            // Sort by name for better usability
            products.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));

            // Update the table
            productTable.setItems(products);
            productTable.refresh();

            System.out.println("Product table refreshed with " + products.size() + " products");
        } catch (Exception e) {
            System.err.println("Error refreshing product table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates and exports an analytics report to a CSV file
     * 
     * @param file The file to export to
     */
    private void generateAnalyticsReport(File file) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            // Write CSV header
            writer.println("Fashion Store Analytics Report," + java.time.LocalDateTime.now());
            writer.println();

            // Product statistics
            List<Product> products = dataManager.getAllProducts();
            writer.println("PRODUCT STATISTICS");
            writer.println("Total Products," + products.size());

            int totalStock = products.stream().mapToInt(Product::getStockQuantity).sum();
            writer.println("Total Stock," + totalStock);

            double totalValue = products.stream()
                    .mapToDouble(p -> p.getPrice().doubleValue() * p.getStockQuantity())
                    .sum();
            writer.println("Inventory Value,$" + String.format("%.2f", totalValue));

            long lowStockCount = products.stream().filter(p -> p.getStockQuantity() < 5).count();
            writer.println("Low Stock Items," + lowStockCount);

            writer.println("Most Popular Category," + getMostPopularCategory(products));
            writer.println();

            // Category breakdown
            writer.println("CATEGORY BREAKDOWN");
            Map<String, Long> categoryCounts = products.stream()
                    .filter(p -> p.getCategory() != null && !p.getCategory().isEmpty())
                    .collect(java.util.stream.Collectors.groupingBy(
                            Product::getCategory, java.util.stream.Collectors.counting()));

            writer.println("Category,Product Count,Percentage");
            for (Map.Entry<String, Long> entry : categoryCounts.entrySet()) {
                double percentage = (double) entry.getValue() / products.size() * 100;
                writer.println(entry.getKey() + "," + entry.getValue() + "," +
                        String.format("%.1f%%", percentage));
            }
            writer.println();

            // User statistics
            List<User> users = dataManager.getAllUsers();
            writer.println("USER STATISTICS");
            writer.println("Total Users," + users.size());

            // Average wardrobe size
            double avgWardrobeSize = users.stream()
                    .mapToDouble(u -> u.getWardrobeItemIds().size())
                    .average()
                    .orElse(0);
            writer.println("Average Wardrobe Size," + String.format("%.1f", avgWardrobeSize));

            // Average outfits per user
            double avgOutfitsPerUser = users.stream()
                    .mapToDouble(u -> u.getOutfitIds().size())
                    .average()
                    .orElse(0);
            writer.println("Average Outfits Per User," + String.format("%.1f", avgOutfitsPerUser));
            writer.println();

            // Detailed product listing
            writer.println("PRODUCT INVENTORY DETAILS");
            writer.println("Product ID,Name,Category,Price,Stock,Value");
            for (Product product : products) {
                double productValue = product.getPrice().doubleValue() * product.getStockQuantity();
                writer.println(
                        product.getProductId() + "," +
                                escapeCSV(product.getName()) + "," +
                                escapeCSV(product.getCategory()) + "," +
                                "$" + product.getPrice() + "," +
                                product.getStockQuantity() + "," +
                                "$" + String.format("%.2f", productValue));
            }
        } catch (java.io.FileNotFoundException e) {
            throw new RuntimeException("Failed to create report file: " + e.getMessage(), e);
        }
    }

    /**
     * Escapes a string for CSV format (adds quotes if needed and escapes quotes)
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // Replace quotes with double quotes
            value = value.replace("\"", "\"\"");
            // Wrap in quotes
            return "\"" + value + "\"";
        }

        return value;
    }

    // Helper method to find the most popular category
    private String getMostPopularCategory(List<Product> products) {
        Map<String, Long> categoryCounts = products.stream()
                .filter(p -> p.getCategory() != null && !p.getCategory().isEmpty())
                .collect(Collectors.groupingBy(
                        Product::getCategory, Collectors.counting()));

        if (categoryCounts.isEmpty()) {
            return "N/A";
        }

        return categoryCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    /**
     * Adds a statistic row to a grid pane with enhanced styling
     * 
     * @param grid  The grid to add to
     * @param row   The row index
     * @param label The statistic label
     * @param value The statistic value
     */
    private void addStatisticToGrid(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("stats-label");

        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("stats-value");

        // Create a container for the value with animated hover effect
        HBox valueContainer = new HBox();
        valueContainer.setAlignment(Pos.CENTER_LEFT);
        valueContainer.setPadding(new Insets(5, 10, 5, 10));
        valueContainer.getChildren().add(valueNode);

        // Add hover effect - this will be styled by CSS
        valueContainer.getStyleClass().add("stats-value-container");

        // Determine if this is a monetary value and add appropriate styling
        if (value.startsWith("$")) {
            valueContainer.getStyleClass().add("monetary-value");
        }

        // Determine if this is a count that might need attention (low stock)
        if (label.contains("Low Stock")) {
            try {
                int count = Integer.parseInt(value);
                if (count > 0) {
                    valueContainer.getStyleClass().add("attention-value");
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }

        grid.add(labelNode, 0, row);
        grid.add(valueContainer, 1, row);
    }

    /**
     * Imports products from a JSON array into the database
     * 
     * @param productsArray The JSON array containing product data
     * @return The number of products successfully imported
     */
    private int importProducts(JsonParser.JsonArray productsArray) {
        int importedCount = 0;

        for (int i = 0; i < productsArray.length(); i++) {
            try {
                JsonParser.JsonObject productObj = productsArray.getJsonObject(i);

                // Extract required fields
                String productId = productObj.getString("productId");
                String name = productObj.getString("name");
                String category = productObj.getString("category");

                // Skip if required fields are missing
                if (name.isEmpty() || category.isEmpty()) {
                    System.err.println("Skipping product with empty name or category");
                    continue;
                }

                // Extract numeric fields
                BigDecimal price = new BigDecimal("0.00");
                if (productObj.has("price")) {
                    price = new BigDecimal(productObj.getString("price"));
                }

                int stockQuantity = productObj.getInt("stockQuantity");
                boolean visible = productObj.getBoolean("visible");

                // Check if product already exists
                Product existingProduct = dataManager.getProduct(productId);
                if (existingProduct != null) {
                    // Update existing product
                    existingProduct.setName(name);
                    existingProduct.setCategory(category);
                    existingProduct.setPrice(price);

                    // Update optional fields if present
                    if (productObj.has("description")) {
                        existingProduct.setDescription(productObj.getString("description"));
                    }

                    if (productObj.has("brand")) {
                        existingProduct.setBrand(productObj.getString("brand"));
                    }

                    if (productObj.has("color")) {
                        existingProduct.setColor(productObj.getString("color"));
                    }

                    if (productObj.has("size")) {
                        existingProduct.setSize(productObj.getString("size"));
                    }

                    if (productObj.has("material")) {
                        existingProduct.setMaterial(productObj.getString("material"));
                    }

                    existingProduct.setStockQuantity(stockQuantity);
                    existingProduct.setVisible(visible);

                    // Set image path, ensuring every product has an image
                    if (productObj.has("imagePath")) {
                        String imagePath = productObj.getString("imagePath");
                        if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("null")) {
                            existingProduct.setImagePath(imagePath);
                        } else {
                            // Set default image if none provided or if null/empty
                            existingProduct.setImagePath("/images/default-product.png");
                        }
                    } else {
                        // No image path in the JSON, set default
                        existingProduct.setImagePath("/images/default-product.png");
                    }

                    dataManager.updateProduct(existingProduct);
                } else {
                    // Create new product
                    Product newProduct = new Product(name, category, price);
                    newProduct.setProductId(productId);

                    // Set optional fields if present
                    if (productObj.has("description")) {
                        newProduct.setDescription(productObj.getString("description"));
                    }

                    if (productObj.has("brand")) {
                        newProduct.setBrand(productObj.getString("brand"));
                    }

                    if (productObj.has("color")) {
                        newProduct.setColor(productObj.getString("color"));
                    }

                    if (productObj.has("size")) {
                        newProduct.setSize(productObj.getString("size"));
                    }

                    if (productObj.has("material")) {
                        newProduct.setMaterial(productObj.getString("material"));
                    }

                    newProduct.setStockQuantity(stockQuantity);
                    newProduct.setVisible(visible);

                    // Set image path, ensuring every product has an image
                    if (productObj.has("imagePath")) {
                        String imagePath = productObj.getString("imagePath");
                        if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("null")) {
                            newProduct.setImagePath(imagePath);
                        } else {
                            // Set default image if none provided or if null/empty
                            newProduct.setImagePath("/images/default-product.png");
                        }
                    } else {
                        // No image path in the JSON, set default
                        newProduct.setImagePath("/images/default-product.png");
                    }

                    dataManager.addProduct(newProduct);
                }

                importedCount++;
            } catch (Exception e) {
                System.err.println("Error importing product: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return importedCount;
    }

    /**
     * Imports users from a JSON array into the database
     * 
     * @param usersArray The JSON array containing user data
     * @return The number of users successfully imported
     */
    private int importUsers(JsonParser.JsonArray usersArray) {
        int importedCount = 0;

        for (int i = 0; i < usersArray.length(); i++) {
            try {
                JsonParser.JsonObject userObj = usersArray.getJsonObject(i);

                // Extract required fields
                String userId = userObj.getString("userId");
                String username = userObj.getString("username");
                String email = userObj.getString("email");

                // Skip if required fields are missing
                if (username.isEmpty() || email.isEmpty()) {
                    System.err.println("Skipping user with empty username or email");
                    continue;
                }

                // Check if user already exists - by ID or username
                User existingUser = dataManager.getUser(userId);
                if (existingUser == null) {
                    existingUser = dataManager.getUserByUsername(username);
                }

                if (existingUser != null) {
                    // Update existing user
                    existingUser.setEmail(email);

                    // Update optional fields if present
                    if (userObj.has("firstName")) {
                        existingUser.setFirstName(userObj.getString("firstName"));
                    }

                    if (userObj.has("lastName")) {
                        existingUser.setLastName(userObj.getString("lastName"));
                    }

                    // Import wardrobe items if present
                    if (userObj.has("wardrobeItems")) {
                        JsonParser.JsonArray wardrobeArray = userObj.getJsonArray("wardrobeItems");
                        for (int j = 0; j < wardrobeArray.length(); j++) {
                            String productId = wardrobeArray.getString(j);
                            if (dataManager.getProduct(productId) != null) {
                                existingUser.addToWardrobe(productId);
                            }
                        }
                    }

                    // Import outfit IDs if present
                    if (userObj.has("outfits")) {
                        JsonParser.JsonArray outfitArray = userObj.getJsonArray("outfits");
                        for (int j = 0; j < outfitArray.length(); j++) {
                            String outfitId = outfitArray.getString(j);
                            existingUser.addOutfit(outfitId);
                        }
                    }

                    // We don't have access to saveUsers(), so we'll save all data at the end
                } else {
                    // For new users, we need to provide a password - use a default one
                    // that will need to be reset by admin
                    String defaultPassword = "password123";

                    // Create new user
                    User newUser = new User(username, email, defaultPassword);
                    newUser.setUserId(userId);

                    // Set optional fields if present
                    if (userObj.has("firstName")) {
                        newUser.setFirstName(userObj.getString("firstName"));
                    }

                    if (userObj.has("lastName")) {
                        newUser.setLastName(userObj.getString("lastName"));
                    }

                    // Import wardrobe items if present
                    if (userObj.has("wardrobeItems")) {
                        JsonParser.JsonArray wardrobeArray = userObj.getJsonArray("wardrobeItems");
                        for (int j = 0; j < wardrobeArray.length(); j++) {
                            String productId = wardrobeArray.getString(j);
                            if (dataManager.getProduct(productId) != null) {
                                newUser.addToWardrobe(productId);
                            }
                        }
                    }

                    // Import outfit IDs if present
                    if (userObj.has("outfits")) {
                        JsonParser.JsonArray outfitArray = userObj.getJsonArray("outfits");
                        for (int j = 0; j < outfitArray.length(); j++) {
                            String outfitId = outfitArray.getString(j);
                            newUser.addOutfit(outfitId);
                        }
                    }

                    dataManager.addUser(newUser);
                }

                importedCount++;
            } catch (Exception e) {
                System.err.println("Error importing user: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return importedCount;
    }

    /**
     * Imports outfits from a JSON array into the database
     * 
     * @param outfitsArray The JSON array containing outfit data
     * @return The number of outfits successfully imported
     */
    private int importOutfits(JsonParser.JsonArray outfitsArray) {
        int importedCount = 0;

        for (int i = 0; i < outfitsArray.length(); i++) {
            try {
                JsonParser.JsonObject outfitObj = outfitsArray.getJsonObject(i);

                // Extract required fields
                String outfitId = outfitObj.getString("outfitId");
                String name = outfitObj.getString("name");
                String createdBy = outfitObj.getString("createdBy");

                // Skip if required fields are missing or user doesn't exist
                if (name.isEmpty() || createdBy.isEmpty() || dataManager.getUser(createdBy) == null) {
                    System.err.println("Skipping outfit with missing required fields or non-existent user");
                    continue;
                }

                // Check if outfit already exists
                Outfit existingOutfit = dataManager.getOutfit(outfitId);
                if (existingOutfit != null) {
                    // Update existing outfit
                    existingOutfit.setName(name);

                    // Update optional fields if present
                    if (outfitObj.has("description")) {
                        existingOutfit.setDescription(outfitObj.getString("description"));
                    }

                    if (outfitObj.has("season") && !outfitObj.isNull("season")) {
                        try {
                            String seasonStr = outfitObj.getString("season");
                            Outfit.OutfitSeason season = Outfit.OutfitSeason.valueOf(seasonStr.toUpperCase());
                            existingOutfit.setSeason(season);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid season value: " + e.getMessage());
                        }
                    }

                    if (outfitObj.has("occasion") && !outfitObj.isNull("occasion")) {
                        try {
                            String occasionStr = outfitObj.getString("occasion");
                            Outfit.OutfitOccasion occasion = Outfit.OutfitOccasion.valueOf(occasionStr.toUpperCase());
                            existingOutfit.setOccasion(occasion);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid occasion value: " + e.getMessage());
                        }
                    }

                    // Clear and re-import product IDs if present
                    existingOutfit.clearProducts();
                    if (outfitObj.has("productIds")) {
                        JsonParser.JsonArray productsArray = outfitObj.getJsonArray("productIds");
                        for (int j = 0; j < productsArray.length(); j++) {
                            String productId = productsArray.getString(j);
                            if (dataManager.getProduct(productId) != null) {
                                existingOutfit.addProduct(productId);
                            }
                        }
                    }

                    dataManager.updateOutfit(existingOutfit);
                } else {
                    // Create new outfit
                    Outfit newOutfit = new Outfit(createdBy, name);
                    newOutfit.setOutfitId(outfitId);

                    // Set optional fields if present
                    if (outfitObj.has("description")) {
                        newOutfit.setDescription(outfitObj.getString("description"));
                    }

                    if (outfitObj.has("season") && !outfitObj.isNull("season")) {
                        try {
                            String seasonStr = outfitObj.getString("season");
                            Outfit.OutfitSeason season = Outfit.OutfitSeason.valueOf(seasonStr.toUpperCase());
                            newOutfit.setSeason(season);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid season value: " + e.getMessage());
                        }
                    }

                    if (outfitObj.has("occasion") && !outfitObj.isNull("occasion")) {
                        try {
                            String occasionStr = outfitObj.getString("occasion");
                            Outfit.OutfitOccasion occasion = Outfit.OutfitOccasion.valueOf(occasionStr.toUpperCase());
                            newOutfit.setOccasion(occasion);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid occasion value: " + e.getMessage());
                        }
                    }

                    // Import product IDs if present
                    if (outfitObj.has("productIds")) {
                        JsonParser.JsonArray productsArray = outfitObj.getJsonArray("productIds");
                        for (int j = 0; j < productsArray.length(); j++) {
                            String productId = productsArray.getString(j);
                            if (dataManager.getProduct(productId) != null) {
                                newOutfit.addProduct(productId);
                            }
                        }
                    }

                    dataManager.addOutfit(newOutfit);
                }

                importedCount++;
            } catch (Exception e) {
                System.err.println("Error importing outfit: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return importedCount;
    }

    /**
     * Sets default image (default-product.png) for all products without an image
     * path
     */
    @FXML
    public void setDefaultImagesForProducts() {
        List<Product> products = dataManager.getAllProducts();
        int updatedCount = 0;

        for (Product product : products) {
            String imagePath = product.getImagePath();
            if (imagePath == null || imagePath.isEmpty() || imagePath.equals("null")) {
                product.setImagePath("/images/default-product.png");
                dataManager.updateProduct(product);
                updatedCount++;
            } else {
                // Check if the image file exists
                try {
                    String resourcePath = imagePath;
                    if (imagePath.startsWith("/")) {
                        resourcePath = imagePath.substring(1);
                    }

                    boolean imageExists = false;

                    // Check if it's a resource path
                    if (getClass().getResource("/" + resourcePath) != null) {
                        imageExists = true;
                    } else if (imagePath.startsWith("file:")) {
                        // Check if it's a file path
                        File imageFile = new File(imagePath.substring(5));
                        imageExists = imageFile.exists() && imageFile.isFile();
                    } else {
                        // Check as regular file
                        File imageFile = new File(imagePath);
                        imageExists = imageFile.exists() && imageFile.isFile();
                    }

                    if (!imageExists) {
                        System.out.println("Image not found for product " + product.getName() + ": " + imagePath);
                        product.setImagePath("/images/default-product.png");
                        dataManager.updateProduct(product);
                        updatedCount++;
                    }
                } catch (Exception e) {
                    System.out.println(
                            "Error checking image path for product " + product.getName() + ": " + e.getMessage());
                    product.setImagePath("/images/default-product.png");
                    dataManager.updateProduct(product);
                    updatedCount++;
                }
            }
        }

        // Refresh the product table
        refreshProductTable();

        // Refresh the Home view to update product images there too
        WindowManager.refreshHomeView();

        if (updatedCount > 0) {
            setStatus(updatedCount + " products updated with default image");
            SceneManager.showAlert("Default Images Applied",
                    updatedCount + " products have been updated with the default image.");
        } else {
            setStatus("All products already have valid images");
            SceneManager.showAlert("Default Images Applied", "All products already have valid images.");
        }
    }

    /**
     * Creates and shows a comprehensive report dialog with printing capability
     * 
     * @param parentStage The parent stage
     */
    private void createAndShowPrintReportDialog(Stage parentStage) {
        try {
            // Create a new stage for the print dialog
            Stage printStage = new Stage();
            printStage.setTitle("Fashion Store Analytics Report");
            printStage.initModality(Modality.WINDOW_MODAL);
            printStage.initOwner(parentStage);
            printStage.setWidth(800);
            printStage.setHeight(700);

            // Create the root container
            BorderPane root = new BorderPane();
            root.setPadding(new Insets(20));

            // Create a VBox for the report content
            VBox reportContent = new VBox(10);
            reportContent.setPadding(new Insets(0, 0, 20, 0));
            ScrollPane scrollPane = new ScrollPane(reportContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: white;");

            // Report header
            Label reportTitle = new Label("Fashion Store Analytics Report");
            reportTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

            // Date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
            Label dateLabel = new Label("Generated on: " + dateFormat.format(new Date()));
            dateLabel.setStyle("-fx-font-style: italic; -fx-font-size: 14px;");

            // Add a separator
            Separator separator = new Separator();
            separator.setPrefWidth(Double.MAX_VALUE);

            // Add header elements to the report
            reportContent.getChildren().addAll(reportTitle, dateLabel, separator);

            // Add inventory summary section
            Label inventorySectionTitle = new Label("Inventory Summary");
            inventorySectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");
            reportContent.getChildren().add(inventorySectionTitle);

            // Get all products
            List<Product> products = dataManager.getAllProducts();

            // Calculate inventory statistics
            int totalProducts = products.size();
            int outOfStockProducts = 0;
            int lowStockProducts = 0;
            BigDecimal totalInventoryValue = BigDecimal.ZERO;
            Map<String, Integer> productsByCategory = new HashMap<>();

            for (Product product : products) {
                // Use stockQuantity instead of quantity
                int stockQuantity = product.getStockQuantity();
                if (stockQuantity == 0) {
                    outOfStockProducts++;
                } else if (stockQuantity < 5) {
                    lowStockProducts++;
                }

                BigDecimal productValue = product.getPrice().multiply(new BigDecimal(stockQuantity));
                totalInventoryValue = totalInventoryValue.add(productValue);

                // Count by category
                String category = product.getCategory();
                productsByCategory.put(category, productsByCategory.getOrDefault(category, 0) + 1);
            }

            // Create an inventory statistics grid
            GridPane inventoryStats = new GridPane();
            inventoryStats.setHgap(20);
            inventoryStats.setVgap(10);
            inventoryStats.setPadding(new Insets(10));
            inventoryStats.setStyle("-fx-background-color: #f8f8f8; -fx-background-radius: 5px;");

            // Add statistics to the grid
            addReportStatistic(inventoryStats, 0, "Total Products:", String.valueOf(totalProducts));
            addReportStatistic(inventoryStats, 1, "Out of Stock Products:", String.valueOf(outOfStockProducts));
            addReportStatistic(inventoryStats, 2, "Low Stock Products:", String.valueOf(lowStockProducts));
            addReportStatistic(inventoryStats, 3, "Total Inventory Value:",
                    "$" + totalInventoryValue.setScale(2, RoundingMode.HALF_UP));

            reportContent.getChildren().add(inventoryStats);

            // Add product category breakdown
            Label categorySectionTitle = new Label("Products by Category");
            categorySectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 15 0 5 0;");
            reportContent.getChildren().add(categorySectionTitle);

            // Create a table for category distribution
            TableView<Map.Entry<String, Integer>> categoryTable = new TableView<>();
            categoryTable.setStyle("-fx-pref-height: 200px;");

            TableColumn<Map.Entry<String, Integer>, String> categoryColumn = new TableColumn<>("Category");
            categoryColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
            categoryColumn.setPrefWidth(200);

            TableColumn<Map.Entry<String, Integer>, Integer> countColumn = new TableColumn<>("Product Count");
            countColumn.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().getValue()).asObject());
            countColumn.setPrefWidth(150);

            categoryTable.getColumns().addAll(categoryColumn, countColumn);

            // Add data to the table
            List<Map.Entry<String, Integer>> categoryEntries = new ArrayList<>(productsByCategory.entrySet());
            categoryTable.setItems(FXCollections.observableArrayList(categoryEntries));

            reportContent.getChildren().add(categoryTable);

            // Add user statistics section
            Label userSectionTitle = new Label("User Statistics");
            userSectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 15 0 5 0;");

            List<User> users = dataManager.getAllUsers();
            int totalUsers = users.size();
            int bannedUsers = 0;

            for (User user : users) {
                if (user.isBanned()) {
                    bannedUsers++;
                }
            }

            // User statistics grid
            GridPane userStats = new GridPane();
            userStats.setHgap(20);
            userStats.setVgap(10);
            userStats.setPadding(new Insets(10));
            userStats.setStyle("-fx-background-color: #f8f8f8; -fx-background-radius: 5px;");

            addReportStatistic(userStats, 0, "Total Registered Users:", String.valueOf(totalUsers));
            addReportStatistic(userStats, 1, "Active Users:", String.valueOf(totalUsers - bannedUsers));
            addReportStatistic(userStats, 2, "Banned Users:", String.valueOf(bannedUsers));

            reportContent.getChildren().addAll(userSectionTitle, userStats);

            // Add outfit statistics section - if Outfit class exists
            try {
                Label outfitSectionTitle = new Label("Outfit Statistics");
                outfitSectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 15 0 5 0;");

                // Find user outfits
                Map<String, Integer> outfitsPerUser = new HashMap<>();
                int totalOutfits = 0;

                // Count outfits per user based on wardrobe items
                for (User user : users) {
                    int outfitCount = user.getOutfitIds() != null ? user.getOutfitIds().size() : 0;
                    totalOutfits += outfitCount;
                    if (outfitCount > 0) {
                        outfitsPerUser.put(user.getUsername(), outfitCount);
                    }
                }

                // Outfit statistics grid
                GridPane outfitStats = new GridPane();
                outfitStats.setHgap(20);
                outfitStats.setVgap(10);
                outfitStats.setPadding(new Insets(10));
                outfitStats.setStyle("-fx-background-color: #f8f8f8; -fx-background-radius: 5px;");

                addReportStatistic(outfitStats, 0, "Total Outfits Created:", String.valueOf(totalOutfits));

                reportContent.getChildren().addAll(outfitSectionTitle, outfitStats);

                // Add a table for top users by outfit creation
                if (!outfitsPerUser.isEmpty()) {
                    Label topUsersTitle = new Label("Top Users by Outfit Creation");
                    topUsersTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

                    TableView<Map.Entry<String, Integer>> outfitUserTable = new TableView<>();
                    outfitUserTable.setStyle("-fx-pref-height: 150px;");

                    TableColumn<Map.Entry<String, Integer>, String> userColumn = new TableColumn<>("Username");
                    userColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
                    userColumn.setPrefWidth(200);

                    TableColumn<Map.Entry<String, Integer>, Integer> outfitCountColumn = new TableColumn<>(
                            "Number of Outfits");
                    outfitCountColumn
                            .setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().getValue()).asObject());
                    outfitCountColumn.setPrefWidth(150);

                    outfitUserTable.getColumns().addAll(userColumn, outfitCountColumn);

                    // Sort and get top users
                    List<Map.Entry<String, Integer>> outfitEntries = new ArrayList<>(outfitsPerUser.entrySet());
                    outfitEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                    // Only show top 5
                    if (outfitEntries.size() > 5) {
                        outfitEntries = outfitEntries.subList(0, 5);
                    }

                    outfitUserTable.setItems(FXCollections.observableArrayList(outfitEntries));

                    reportContent.getChildren().addAll(topUsersTitle, outfitUserTable);
                }
            } catch (Exception ex) {
                // Skip outfit section if there's an error
                System.out.println("Skipping outfit section: " + ex.getMessage());
            }

            // Add button controls
            HBox controls = new HBox(10);
            controls.setAlignment(Pos.CENTER_RIGHT);
            controls.setPadding(new Insets(20, 0, 0, 0));

            Button printButton = new Button("Print Report");
            printButton.getStyleClass().add("action-button");
            printButton.setOnAction(e -> {
                try {
                    PrinterJob job = PrinterJob.createPrinterJob();
                    if (job != null && job.showPrintDialog(printStage)) {
                        // Set status
                        setStatus("Printing report...");

                        // Print the report content
                        boolean success = job.printPage(reportContent);
                        if (success) {
                            job.endJob();
                            setStatus("Report printed successfully");
                            printStage.close();
                        } else {
                            setStatus("Printing failed");
                        }
                    }
                } catch (Exception ex) {
                    SceneManager.showErrorAlert("Print Error", "Failed to print report: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            Button exportButton = new Button("Export Report");
            exportButton.getStyleClass().add("secondary-button");
            exportButton.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Report");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("HTML Files", "*.html"));
                fileChooser.setInitialFileName("FashionStore-Report-" +
                        new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".html");

                File file = fileChooser.showSaveDialog(printStage);
                if (file != null) {
                    try {
                        // Generate HTML report
                        StringBuilder html = new StringBuilder();
                        html.append("<!DOCTYPE html>\n");
                        html.append("<html>\n");
                        html.append("<head>\n");
                        html.append("<title>Fashion Store Analytics Report</title>\n");
                        html.append("<style>\n");
                        html.append("body { font-family: Arial, sans-serif; margin: 40px; }\n");
                        html.append("h1 { color: #333; }\n");
                        html.append("h2 { color: #555; margin-top: 30px; }\n");
                        html.append(".date { color: #777; font-style: italic; margin-bottom: 30px; }\n");
                        html.append("table { border-collapse: collapse; width: 100%; margin: 20px 0; }\n");
                        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
                        html.append("th { background-color: #f2f2f2; }\n");
                        html.append("tr:nth-child(even) { background-color: #f9f9f9; }\n");
                        html.append(".stats { background-color: #f5f5f5; padding: 15px; border-radius: 5px; }\n");
                        html.append("</style>\n");
                        html.append("</head>\n");
                        html.append("<body>\n");
                        
                        // Add title
                        html.append("<h1>Fashion Store Analytics Report</h1>\n");
                        html.append("<p class=\"date\">Generated on: " + 
                            new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a").format(new Date()) + "</p>\n");
                        
                        // Add inventory summary
                        html.append("<h2>Inventory Summary</h2>\n");
                        
                        // Get all products
                        List<Product> productsForReport = dataManager.getAllProducts();
                        
                        // Calculate inventory statistics
                        int totalProductsCount = productsForReport.size();
                        int outOfStockCount = 0;
                        int lowStockCount = 0;
                        BigDecimal totalValue = BigDecimal.ZERO;
                        
                        for (Product product : productsForReport) {
                            int stockQuantity = product.getStockQuantity();
                            if (stockQuantity == 0) {
                                outOfStockCount++;
                            } else if (stockQuantity < 5) {
                                lowStockCount++;
                            }
                            
                            BigDecimal productValue = product.getPrice().multiply(new BigDecimal(stockQuantity));
                            totalValue = totalValue.add(productValue);
                        }
                        
                        // Add statistics
                        html.append("<div class=\"stats\">\n");
                        html.append("<p><strong>Total Products:</strong> " + totalProductsCount + "</p>\n");
                        html.append("<p><strong>Out of Stock Products:</strong> " + outOfStockCount + "</p>\n");
                        html.append("<p><strong>Low Stock Products:</strong> " + lowStockCount + "</p>\n");
                        html.append("<p><strong>Total Inventory Value:</strong> $" + 
                            totalValue.setScale(2, RoundingMode.HALF_UP) + "</p>\n");
                        html.append("</div>\n");
                        
                        // Add category breakdown
                        html.append("<h2>Products by Category</h2>\n");
                        
                        // Create category table
                        html.append("<table>\n");
                        html.append("<tr><th>Category</th><th>Product Count</th></tr>\n");
                        
                        // Count products by category
                        Map<String, Integer> categoryCount = new HashMap<>();
                        for (Product product : productsForReport) {
                            String category = product.getCategory();
                            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
                        }
                        
                        // Add data to table
                        for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
                            html.append("<tr><td>" + entry.getKey() + "</td><td>" + entry.getValue() + "</td></tr>\n");
                        }
                        
                        html.append("</table>\n");
                        
                        // Add user statistics
                        html.append("<h2>User Statistics</h2>\n");
                        
                        List<User> usersForReport = dataManager.getAllUsers();
                        int totalUsersCount = usersForReport.size();
                        int bannedUsersCount = 0;
                        
                        for (User user : usersForReport) {
                            if (user.isBanned()) {
                                bannedUsersCount++;
                            }
                        }
                        
                        html.append("<div class=\"stats\">\n");
                        html.append("<p><strong>Total Registered Users:</strong> " + totalUsersCount + "</p>\n");
                        html.append("<p><strong>Active Users:</strong> " + (totalUsersCount - bannedUsersCount) + "</p>\n");
                        html.append("<p><strong>Banned Users:</strong> " + bannedUsersCount + "</p>\n");
                        html.append("</div>\n");
                        
                        // Close HTML
                        html.append("</body>\n");
                        html.append("</html>");
                        
                        // Write to file
                        try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                            writer.write(html.toString());
                        }
                        
                        setStatus("Analytics report exported to: " + file.getAbsolutePath());
                        SceneManager.showAlert("Export Successful", 
                            "Analytics report has been exported to:\n" + file.getAbsolutePath() + 
                            "\n\nYou can open this file in your web browser to view the report.");
                    } catch (Exception ex) {
                        SceneManager.showErrorAlert("Export Error", 
                            "Failed to export report: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            });

            Button closeButton = new Button("Close");
            closeButton.getStyleClass().add("secondary-button");
            closeButton.setOnAction(e -> printStage.close());

            controls.getChildren().addAll(printButton, exportButton, closeButton);

            // Set the layout
            root.setCenter(scrollPane);
            root.setBottom(controls);

            // Create the scene and show
            Scene scene = new Scene(root);
            // Get dark mode setting from the scene or environment
            boolean isDarkMode = false;
            try {
                // Check if the parent scene has dark mode enabled
                Scene parentScene = parentStage.getScene();
                if (parentScene != null && parentScene.getRoot() != null) {
                    Object themeProperty = parentScene.getRoot().getProperties().get("theme");
                    isDarkMode = "dark".equals(themeProperty);
                }
            } catch (Exception ex) {
                System.out.println("Could not determine theme from parent: " + ex.getMessage());
            }

            // Use the current application theme
            String currentTheme = isDarkMode ? "/styles/dark-theme.css" : "/styles/application.css";
            scene.getStylesheets().add(getClass().getResource(currentTheme).toExternalForm());
            printStage.setScene(scene);
            printStage.show();

            // Set status
            setStatus("Generated analytics report");

        } catch (Exception ex) {
            SceneManager.showErrorAlert("Report Error", "Failed to generate report: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Adds a statistic to the report grid
     * 
     * @param grid  The grid to add the statistic to
     * @param row   The row index
     * @param label The label for the statistic
     * @param value The value of the statistic
     */
    private void addReportStatistic(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold;");

        Label valueNode = new Label(value);

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    /**
     * Toggles between dark and light mode
     */
    @FXML
    public void toggleTheme() {
        try {
            if (productTable.getScene() != null) {
                String currentTheme = "light"; // Default theme
                if (productTable.getScene().getRoot().getProperties().get("theme") != null) {
                    currentTheme = (String) productTable.getScene().getRoot().getProperties().get("theme");
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
                
                setStatus("Theme changed to " + (currentTheme.equals("dark") ? "light" : "dark") + " mode");
            }
        } catch (Exception e) {
            System.err.println("Failed to toggle theme: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
