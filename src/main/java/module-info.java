module com.example.mailbridge {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.mail;


    opens com.example.mailbridge to javafx.fxml;
    exports com.example.mailbridge;
}