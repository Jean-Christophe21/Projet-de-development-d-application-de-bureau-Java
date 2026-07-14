module tg.univ.lome.epl.gestionpharmacie {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;


    opens tg.univ.lome.epl.gestionpharmacie to javafx.fxml;
    exports tg.univ.lome.epl.gestionpharmacie;
}
