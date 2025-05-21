package com.example.fashionstoreclothing;

import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class AdminController implements Initializable, UserAware {
    @FXML
    private TextField historyFilterField;
    @FXML
    private Label statusLabel;
    @FXML
    private TableView<User> activeUsersTable;
    @FXML
    private TableView<LoginRecord> loginHistoryTable;
    @FXML
    private Label dateTimeLabel;
    @FXML
    private TextField filterField;
    @FXML
    private TableColumn<LoginRecord, LocalDateTime> loginTimeColumn;
    @FXML
    private TableColumn<LoginRecord, LocalDateTime> logoutTimeColumn;

    private UserService userService = UserService.getInstance();
    private Logger logger = new Logger(AdminController.class);
    private User currentUser;
    private FilteredList<LoginRecord> filteredLoginHistory;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the date/time label
        updateDateTime();

        // Load initial data
        loadActiveUsers();
        loadLoginHistory();

        // Set up filter listener
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredLoginHistory != null) {
                filteredLoginHistory.setPredicate(record -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    return record.getUsername().toLowerCase().contains(lowerCaseFilter);
                });
            }
        });

        // Set cell factories for loginTimeColumn and logoutTimeColumn
        loginTimeColumn.setCellFactory(column -> new TableCell<LoginRecord, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    setStyle("-fx-text-fill: black;");
                }
            }
        });

        logoutTimeColumn.setCellFactory(column -> new TableCell<LoginRecord, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Active");
                    setStyle("-fx-text-fill: green;");
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    setStyle("-fx-text-fill: black;");
                }
            }
        });
    }

    private void updateDateTime() {
        // Get current date/time and update the label
        dateTimeLabel.setText("Current Date and Time: " + LocalDateTime.now().toString());
    }

    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void handleClearFilter() {
        filterField.clear();
        if (filteredLoginHistory != null) {
            filteredLoginHistory.setPredicate(record -> true);
        }
    }

    @FXML
    public void handleReturnToMain() {
        try {
            ClothingStoreApp.loadNewScene("InventoryView.fxml", "Fashion Boutique - Inventory", 800, 600);
            logger.info("Admin returned to main view: " + currentUser.getUsername());
        } catch (IOException e) {
            statusLabel.setText("Error returning to main view: " + e.getMessage());
            logger.error("Error returning to main view", e);
        }
    }

    @FXML
    public void handleLogout() {
        try {
            if (currentUser != null) {
                userService.recordLogout(currentUser.getUsername());
                logger.info("Admin logged out: " + currentUser.getUsername());
            }
            ClothingStoreApp.setCurrentUser(null);
            ClothingStoreApp.loadNewScene("LoginView.fxml", "Fashion Boutique - Login", 600, 400);
        } catch (IOException e) {
            statusLabel.setText("Error logging out: " + e.getMessage());
            logger.error("Error logging out", e);
        }
    }

    @FXML
    public void handleRefreshData(ActionEvent event) {
        try {
            updateDateTime();
            loadActiveUsers();
            loadLoginHistory();
            statusLabel.setText("Data refreshed successfully.");
            logger.info("Admin refreshed data.");
        } catch (Exception e) {
            statusLabel.setText("Error refreshing data: " + e.getMessage());
            logger.error("Error refreshing data", e);
        }
    }

    private void loadActiveUsers() {
        activeUsersTable.setItems(userService.getActiveUsers());
    }

    private void loadLoginHistory() {
        var history = userService.getLoginHistory();
        filteredLoginHistory = new FilteredList<>(history, p -> true);
        loginHistoryTable.setItems(filteredLoginHistory);
    }
}