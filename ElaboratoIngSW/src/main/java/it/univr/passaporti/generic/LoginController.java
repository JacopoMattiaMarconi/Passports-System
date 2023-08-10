package it.univr.passaporti.generic;

import it.univr.passaporti.Main;
import it.univr.passaporti.generic.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController{
    private Connection conn = null;
    private static String urldb="jdbc:mysql://localhost:3306/passaporti";
    private static String userdb="root";
    private static String passdb="";

    @FXML
    private TextField cfField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private TextField idFieldAdmin;

    @FXML
    private PasswordField passwordFieldAdmin;

    @FXML
    private Button loginButtonAdmin;

    @FXML
    protected void onBackButtonClick() throws IOException {
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/home.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }

    @FXML
    protected void loginCitizen() throws IOException {
        Window owner = loginButton.getScene().getWindow();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            PreparedStatement stmt =conn.prepareStatement("SELECT password, salt FROM cittadini WHERE codiceFiscale=?");
            stmt.setString(1, cfField.getText().toUpperCase());
            ResultSet rs = stmt.executeQuery();

            if(rs.next()){
                String pswCitizen = rs.getString("password");
                String salt = rs.getString("salt");
                String psw = encrypt(passwordField.getText(), salt);
                if(!pswCitizen.equals(psw)){
                    showAlert(Alert.AlertType.WARNING, owner, "Errore!", "Codice utente o password errati, riprova.");
                    return;
                }
                UserSession.getInstace(cfField.getText().toUpperCase());
                Stage primaryStage = (Stage) Main.getScene().getWindow();
                Parent newRoot = FXMLLoader.load(Main.class.getResource("views/citizen/homeCitizen.fxml"));
                primaryStage.getScene().setRoot(newRoot);
            }else{
                showAlert(Alert.AlertType.WARNING, owner, "Errore!", "Codice utente o password errati, riprova.");
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    @FXML
    protected void loginAdmin() throws IOException {
        Window owner = loginButtonAdmin.getScene().getWindow();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            PreparedStatement stmt =conn.prepareStatement("SELECT password, salt FROM personale WHERE idPersonale=?");
            stmt.setString(1, idFieldAdmin.getText().toUpperCase());
            ResultSet rs = stmt.executeQuery();

            if(rs.next()){
                String pswAdmin = rs.getString("password");
                String salt = rs.getString("salt");
                String psw = encrypt(passwordFieldAdmin.getText(), salt);
                if(!pswAdmin.equals(psw)){
                    showAlert(Alert.AlertType.WARNING, owner, "Errore!", "Codice utente o password errata, riprova.");
                    return;
                }
                //UserSession.getInstace(idFieldAdmin.getText().toUpperCase());
                Stage primaryStage = (Stage) Main.getScene().getWindow();
                Parent newRoot = FXMLLoader.load(Main.class.getResource("views/administration/homeAdministration.fxml"));
                primaryStage.getScene().setRoot(newRoot);
            }else{
                showAlert(Alert.AlertType.WARNING, owner, "Errore!", "Codice utente o password errata, riprova.");
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    @FXML
    protected void checkFieldsCitizens() {
        if(cfField.getText().length() == 16 && !(passwordField.getText().isEmpty())){
            loginButton.setDisable(false);
        }else{
            loginButton.setDisable(true);
        }
    }

    @FXML
    protected void checkFieldsAdmin() {
        if(idFieldAdmin.getText().length() == 5 && !(passwordFieldAdmin.getText().isEmpty())){
            loginButtonAdmin.setDisable(false);
        }else{
            loginButtonAdmin.setDisable(true);
        }
    }

    @FXML
    protected void onRegistrationLinkClick() throws IOException {
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/citizen/registrationCitizen.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }

    private static void showAlert(Alert.AlertType alertType, Window owner, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.show();
    }

    protected static String encrypt(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        String psw = new String(hash);
        psw += salt;
        return new String(digest.digest(psw.getBytes(StandardCharsets.UTF_8)));
    }
}