module com.fashionstore {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;

    // JDBC modules
    requires java.sql;

    // HikariCP - use automatic modules approach since HikariCP isn't fully
    // modularized
    requires static com.zaxxer.hikari;

    // Allow JavaFX to access all packages
    opens com.fashionstore.application to javafx.fxml, javafx.graphics;
    opens com.fashionstore.controllers to javafx.fxml;
    opens com.fashionstore.models to javafx.base;
    opens com.fashionstore.ui.components to javafx.fxml;
    opens com.fashionstore.ai to javafx.fxml;

    // Export packages for reflection
    exports com.fashionstore.application;
    exports com.fashionstore.controllers;
    exports com.fashionstore.models;
    exports com.fashionstore.storage;
    exports com.fashionstore.utils;
    exports com.fashionstore.ui.components;
    exports com.fashionstore.ai;
}