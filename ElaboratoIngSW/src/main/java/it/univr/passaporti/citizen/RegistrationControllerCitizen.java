package it.univr.passaporti.citizen;

import it.univr.passaporti.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Random;

public class RegistrationControllerCitizen {

    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private TextField cfField;

    @FXML
    private DatePicker dateField;

    @FXML
    private TextField placeField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField passwordConfirmField;

    @FXML
    private Button registrationButton;

    private Connection conn = null;
    private static String urldb="jdbc:mysql://localhost:3306/passaporti";
    private static String userdb="root";
    private static String passdb="";

    @FXML
    protected void onBackButtonClick() throws IOException {
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/citizen/loginCitizen.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }

    @FXML
    protected void registration() throws IOException, NoSuchAlgorithmException {
        Window owner = registrationButton.getScene().getWindow();
        if(!checkPassword())
            return;
        if(!checkAnagrafica())
            return;
        String salt = generateSalt();
        String psw = encrypt(passwordField.getText(), salt);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);

            PreparedStatement  stmt = conn.prepareStatement("INSERT INTO cittadini (codiceFiscale, nome, cognome, dataNascita, luogoNascita, password, salt) VALUES(?,?,?,?,?,?,?)");
            stmt.setString(1, cfField.getText().toUpperCase());
            stmt.setString(2, nameField.getText().toUpperCase());
            stmt.setString(3, surnameField.getText().toUpperCase());
            stmt.setDate(4, Date.valueOf(dateField.getValue()));
            stmt.setString(5, placeField.getText().toUpperCase());
            stmt.setString(6, psw);
            stmt.setString(7, salt);
            stmt.execute();
            showAlert(Alert.AlertType.INFORMATION,owner, "Successo!", "Utente registrato con successo!");
            stmt.close();
            conn.close();
            onBackButtonClick();
        }
        catch (Exception e){
            showAlert(Alert.AlertType.ERROR,owner, "Errore!", "Errore di registrazione");
            System.out.println(e);
        }
    }

    private static void showAlert(Alert.AlertType alertType, Window owner, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.show();
    }

    @FXML
    private void enableRegistration(){
        if(!(nameField.getText().isEmpty() || surnameField.getText().isEmpty() || cfField.getText().isEmpty() || dateField.getValue() == null || placeField.getText().isEmpty() || passwordField.getText().isEmpty() || passwordConfirmField.getText().isEmpty()))
            registrationButton.setDisable(false);
        else
            registrationButton.setDisable(true);

        if(!passwordField.getText().isEmpty())
            passwordConfirmField.setDisable(false);
        else
            passwordConfirmField.setDisable(true);
    }

    private boolean checkPassword(){
        Window w = registrationButton.getScene().getWindow();

        if(!passwordField.getText().equals(passwordConfirmField.getText())) {
            showAlert(Alert.AlertType.WARNING, w, "Errore!", "Le due password non coincidono!");
            return false;
        }
        if(passwordField.getText().length()<=1){
            showAlert(Alert.AlertType.WARNING, w, "Errore!", "La password deve essere più lunga di 1 carattere!");
            return false;
        }
        return true;
    }

    private boolean checkAnagrafica() throws IOException{
        Window w = registrationButton.getScene().getWindow();
        String cfAnagrafica="";
        String nomeAnagrafica="";
        String cognomeAnagrafica="";
        Date dataNascitaAnagrafica=null;
        String luogoNascitaAnagrafica="";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            PreparedStatement stmt =conn.prepareStatement("SELECT * FROM anagrafiche where codiceFiscale=?");
            stmt.setString(1, cfField.getText().toUpperCase());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cfAnagrafica = rs.getString("codiceFiscale");
                nomeAnagrafica = rs.getString("nome");
                cognomeAnagrafica = rs.getString("cognome");
                dataNascitaAnagrafica = rs.getDate("dataNascita");
                luogoNascitaAnagrafica = rs.getString("luogoNascita");
            }

            if(!(cfField.getText().toUpperCase().equals(cfAnagrafica) && nameField.getText().toUpperCase().equals(nomeAnagrafica) &&
                    surnameField.getText().toUpperCase().equals(cognomeAnagrafica) && /*Date.valueOf(dateField.getValue()).equals(dataNascitaAnagrafica) &&*/
                    placeField.getText().toUpperCase().equals(luogoNascitaAnagrafica))){
                showAlert(Alert.AlertType.WARNING,w, "Errore!", "I dati immessi non corrispondono all'anagrafica del cittadino. Riprovare o contattare gli uffici.");
                return false;
            }

            stmt.close();
            conn.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
        return true;
    }

    private String generateSalt(){
        Random rand = new Random();
        String salt = "";
        for(int i = 0; i < 32; i++){
            salt += (char)(rand.nextInt(126) + 33);
        }
        return salt;
    }

    protected static String encrypt(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        String psw = new String(hash);
        psw += salt;
        return new String(digest.digest(psw.getBytes(StandardCharsets.UTF_8)));
    }
}
