package it.univr.passaporti.generic;

import it.univr.passaporti.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML
    protected void onCitizensButtonClick() throws IOException {
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/citizen/loginCitizen.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }

    @FXML
    protected void onAdministrationButtonClick() throws IOException {
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/Administration/loginAdministration.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }
}