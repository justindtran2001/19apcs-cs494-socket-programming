package com.apcscs494.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ServerApp.class.getResource("server-app.fxml"));

            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("The Magical Wheel (server)");
            stage.setScene(scene);
            stage.show();
        } catch (Exception exception) {
            System.out.println("Exception at setting up JavaFX scene");
            exception.printStackTrace();
            throw exception;
        }
    }

    public static void main(String[] args) {
        launch();
    }

}
