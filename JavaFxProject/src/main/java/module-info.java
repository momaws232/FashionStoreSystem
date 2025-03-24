module com.example.javafxproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.example.fashionstoreclothing to javafx.fxml, javafx.base;
    exports com.example.fashionstoreclothing;
}