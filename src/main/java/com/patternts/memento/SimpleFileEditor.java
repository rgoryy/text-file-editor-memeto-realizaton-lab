package com.patternts.memento;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SimpleFileEditor extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/SimpleFileEditor.fxml"));

        Parent root = loader.load();

        primaryStage.setTitle("Simple Text File Editor");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
