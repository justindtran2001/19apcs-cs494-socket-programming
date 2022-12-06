module com.apcscs494.server {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.apcscs494.server to javafx.fxml;
    exports com.apcscs494.server;
}