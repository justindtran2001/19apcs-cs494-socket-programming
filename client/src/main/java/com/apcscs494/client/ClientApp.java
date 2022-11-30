package com.apcscs494.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;

public class ClientApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource("client-register.fxml"));

            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("The Magical Wheel (client)");
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