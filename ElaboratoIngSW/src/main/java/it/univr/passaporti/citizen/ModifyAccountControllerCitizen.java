package it.univr.passaporti.citizen;

import it.univr.passaporti.Main;
import it.univr.passaporti.generic.UserSession;
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

import static java.lang.Thread.sleep;

public class ModifyAccountControllerCitizen {
    private final static String urldb = "jdbc:mysql://localhost:3306/passaporti";
    private final static String userdb = "root";
    private final static String passdb = "";

    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField newPasswordConfirmField;

    @FXML
    private Label passwordErrorLabel;

    @FXML
    private Button passwordButton;

    @FXML
    private void onLogoutClick() throws IOException {
        UserSession.cleanUserSession();
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/home.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }

    @FXML
    private void onBackButtonClick() throws IOException {
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/citizen/homeCitizen.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }

    @FXML
    private void enablePasswordButton() {
        if (oldPasswordField.getText().isEmpty()) {
            passwordButton.setDisable(true);
        }

        if(newPasswordField.getText().isEmpty() || newPasswordConfirmField.getText().isEmpty()) {
            passwordButton.setDisable(true);
            passwordErrorLabel.setVisible(false);
        } else {
            if(newPasswordField.getText().equals(newPasswordConfirmField.getText())){
                passwordButton.setDisable(false);
                passwordErrorLabel.setVisible(false);
            } else {
                passwordButton.setDisable(true);
                passwordErrorLabel.setVisible(true);
            }

        }

    }

    @FXML
    private void modifyPassword(){
        Window w = passwordButton.getScene().getWindow();
        String codFisc = UserSession.getUserName();

        String oldPassword = "", oldSalt = "";
        String newSalt = "", newPassword = "";
        try {
            Connection conn = DriverManager.getConnection(urldb, userdb, passdb);
            PreparedStatement stmt = conn.prepareStatement("SELECT salt, password FROM cittadini WHERE codiceFiscale = ?");
            stmt.setString(1, codFisc);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                oldSalt = rs.getString("salt");
                oldPassword = rs.getString("password");
            }

            if(!oldPassword.equals(encrypt(oldPasswordField.getText(), oldSalt))) {
                showAlert(Alert.AlertType.WARNING, w, "Errore!", "La vecchia password inserita non è corretta.");
            } else {
                stmt = conn.prepareStatement("UPDATE cittadini SET salt = ?, password = ? WHERE codiceFiscale = ?");
                newSalt = generateSalt();
                newPassword = encrypt(newPasswordField.getText(), newSalt);
                stmt.setString(1, newSalt);
                stmt.setString(2, newPassword);
                stmt.setString(3, codFisc);
                stmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, w, "Successo!", "La password è stata modificata con successo.");
                onBackButtonClick();
            }

            stmt.close();
            conn.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

    private static void showAlert(Alert.AlertType alertType, Window owner, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.show();
    }
}
