module com.example.prokopchuk13 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires java.sql;
    requires java.rmi;

    opens com.example.prokopchuk13 to javafx.fxml;
    exports com.example.prokopchuk13;
}