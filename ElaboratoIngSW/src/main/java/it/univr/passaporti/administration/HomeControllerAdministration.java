package it.univr.passaporti.administration;

import it.univr.passaporti.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeControllerAdministration {

    @FXML
    private Button richiesteButton;

    @FXML
    private Button disponibilitaButton;

    @FXML
    protected void onLogoutClick() throws IOException {
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/home.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }

    @FXML
    protected void onRichiesteButtonClick() throws IOException {
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/administration/requestsAdministration.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }

    @FXML
    protected void onDisponibilitaButtonClick() throws IOException {
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/administration/disponibilityAdministration.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }
}
