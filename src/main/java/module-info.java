module agrticloud {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    opens org.example to javafx.graphics, javafx.fxml, javafx.base, javafx.controls;
    opens org.example.model to javafx.graphics, javafx.fxml, javafx.base, javafx.controls;
    opens org.example.controller to javafx.graphics, javafx.fxml, javafx.base, javafx.controls;
    exports org.example;
    exports org.example.controller;
    exports org.example.model;
}
