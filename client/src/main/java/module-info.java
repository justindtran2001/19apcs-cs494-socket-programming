module com.apcscs494.client {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.apcscs494.client to javafx.fxml;
    exports com.apcscs494.client;
    exports com.apcscs494.client.constants;
    opens com.apcscs494.client.constants to javafx.fxml;
}