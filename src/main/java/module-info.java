module com.example.desencryption {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.desencryption to javafx.fxml;
    exports com.example.desencryption;
}