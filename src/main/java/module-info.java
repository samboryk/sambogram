module pr.sambogram {
    requires javafx.controls;
    requires javafx.fxml;


    opens pr.sambogram to javafx.fxml;
    exports pr.sambogram;
}