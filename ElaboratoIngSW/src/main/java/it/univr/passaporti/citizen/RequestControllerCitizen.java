package it.univr.passaporti.citizen;

import it.univr.passaporti.Main;
import it.univr.passaporti.generic.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.*;

public class RequestControllerCitizen {
    private Connection conn = null;
    private static String urldb = "jdbc:mysql://localhost:3306/passaporti";
    private static String userdb = "root";
    private static String passdb = "";

    @FXML
    private CheckBox primaVoltaCheckBox;

    @FXML
    private CheckBox furtoCheckBox;

    @FXML
    private CheckBox scadenzaCheckBox;

    @FXML
    private CheckBox smarrimentoCheckBox;

    @FXML
    private CheckBox deterioramentoCheckBox;

    @FXML
    private Button conferma;

    private CheckBox selected;

    @FXML
    protected void onLogoutClick() throws IOException {
        UserSession.cleanUserSession();
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/home.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }

    @FXML
    protected void onBackButtonClick() throws IOException {
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/citizen/homeCitizen.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }

    @FXML
    protected void checkSelectedBox(ActionEvent event){
        selected = (CheckBox) event.getSource();
        primaVoltaCheckBox.setSelected(false);
        furtoCheckBox.setSelected(false);
        scadenzaCheckBox.setSelected(false);
        smarrimentoCheckBox.setSelected(false);
        deterioramentoCheckBox.setSelected(false);
        selected.setSelected(true);
        if(selected.getText().isEmpty()){
            conferma.setDisable(true);
        }
        else{
            conferma.setDisable(false);
        }
    }

    @FXML
    protected void onConfermaButton(){
        Window owner = conferma.getScene().getWindow();
        String codFisc = UserSession.getUserName();
        String motivo=selected.getText();
        Date dataOdierna = new Date(System.currentTimeMillis());
        int idSedeAppuntamento = 0;

        //se c'è una richiesta già presente o un ritiro non chiuso non entrare
        if(!(controlloDoppiaRichiesta())) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(urldb, userdb, passdb);
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO richieste (codiceFiscaleRichiedente, motivoRichiesta, dataRichiesta, statoRichiesta) VALUES(?,?,?,?)");
                stmt.setString(1, codFisc);
                stmt.setString(2, motivo);
                stmt.setDate(3, dataOdierna);
                stmt.setString(4, "aperta");
                stmt.execute();
                Stage primaryStage = (Stage) Main.getScene().getWindow();
                Parent newRoot = FXMLLoader.load(Main.class.getResource("views/citizen/disponibilityCitizen.fxml"));
                primaryStage.getScene().setRoot(newRoot);
                stmt.close();
                conn.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        else{
            showAlert(Alert.AlertType.ERROR,owner, "Errore!", "Risulta già presente una richiesta di rilascio del passaporto.");
        }
    }

    protected boolean controlloDoppiaRichiesta(){
        String codFisc = UserSession.getUserName();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM richieste where codiceFiscaleRichiedente=? AND statoRichiesta!='chiusa'");
            stmt.setString(1, codFisc);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String stato = rs.getString("statoRichiesta");
                if(!(stato.equals("chiusa")))
                    return true;
            }
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    private static void showAlert(Alert.AlertType alertType, Window owner, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.show();
    }
}
