module com.example.elaborato {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;



    exports it.univr.passaporti.citizen;
    opens it.univr.passaporti.citizen to javafx.fxml;
    exports it.univr.passaporti.generic;
    opens it.univr.passaporti.generic to javafx.fxml;
    exports it.univr.passaporti.administration;
    opens it.univr.passaporti.administration to javafx.fxml;
    exports it.univr.passaporti;
    opens it.univr.passaporti to javafx.fxml;
}