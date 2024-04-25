module com.example.testmemento {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;


    opens com.patternts.memento to javafx.fxml;
    exports com.patternts.memento;
    exports com.patternts.memento.controller;
    opens com.patternts.memento.controller to javafx.fxml;
}