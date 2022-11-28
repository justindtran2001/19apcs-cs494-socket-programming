module com.apcscs494.themagicalwheelgame {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.apcscs494.themagicalwheelgame to javafx.fxml;
    exports com.apcscs494.themagicalwheelgame;
}