module com.example.aitester {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.aitester to javafx.fxml;
    exports com.example.aitester;
}