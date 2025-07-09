module com.example.aitester {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;


    opens com.example.aitester to javafx.fxml;
    exports com.example.aitester;
}