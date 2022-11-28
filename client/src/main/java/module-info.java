module com.apcscs494.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.apcscs494.server;


    opens com.apcscs494.client to javafx.fxml;
    exports com.apcscs494.client;
}