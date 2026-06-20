module tg.univ.lome.epl.gestionpharmacie {
    requires javafx.controls;
    requires javafx.fxml;

    opens tg.univ.lome.epl.gestionpharmacie to javafx.fxml;
    exports tg.univ.lome.epl.gestionpharmacie;
}
