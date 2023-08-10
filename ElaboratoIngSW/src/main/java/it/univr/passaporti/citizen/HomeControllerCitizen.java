package it.univr.passaporti.citizen;

import it.univr.passaporti.Main;
import it.univr.passaporti.generic.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class HomeControllerCitizen implements Initializable {
    private Connection conn = null;
    private static String urldb = "jdbc:mysql://localhost:3306/passaporti";
    private static String userdb = "root";
    private static String passdb = "";

    @FXML
    private Button richiestaButton;

    @FXML
    private Button ritiroButton;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Tab tabAppuntamenti;

    @FXML
    private Tab tabDocumenti;

    @FXML
    protected void onLogoutClick() throws IOException {
        UserSession.cleanUserSession();
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/home.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }

    @FXML
    protected void onAccountButtonClick() throws IOException {
        Stage primaryStage = (Stage) Main.getScene().getWindow();
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/citizen/modifyAccountCitizen.fxml"));
        primaryStage.getScene().setRoot(newRoot);
    }

    @FXML
    protected void onRichiestaButtonClick() throws IOException {
        Window owner = richiestaButton.getScene().getWindow();
        String s = UserSession.getUserName();
        Date data=null;
        boolean b=false; //usata per saltare alla pagina disponibilità
        boolean a=false; //usata per saltare alla pagina seleziona richiesta
        boolean c=false; //usata per dare errore
        String motivo="",stato="";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM richieste where codiceFiscaleRichiedente=?");
            stmt.setString(1, s);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                data = rs.getDate("dataAppuntamento");
                motivo=rs.getString("motivoRichiesta");
                stato=rs.getString("statoRichiesta");
                if ( (!(motivo.equals("Ritiro passaporto")) && stato.equals("aperta")) && data==null){
                    b=true;
                }
                if((motivo.equals("Ritiro passaporto") && stato.equals("aperta"))){
                    a=true;
                }
                if(stato.equals("pronta") || stato.equals("in elaborazione")){
                    c=true;
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);;
        }
        if(b){
            Stage primaryStage = (Stage) Main.getScene().getWindow();
            Parent newRoot = FXMLLoader.load(Main.class.getResource("views/citizen/disponibilityCitizen.fxml"));
            primaryStage.getScene().setRoot(newRoot);
        }
        else if(a){
            showAlert(Alert.AlertType.ERROR,owner, "Errore!", "Pratica di ritiro passaporto in elaborazione. Andare nella sezione ritiro passaporto.");
        }
        else if(c){
            showAlert(Alert.AlertType.ERROR,owner, "Errore!", "Risulta già presente una richiesta o un ritiro in elaborazione.");
        }
        else{
            Stage primaryStage = (Stage) Main.getScene().getWindow();
            Parent newRoot = FXMLLoader.load(Main.class.getResource("views/citizen/requestCitizen.fxml"));
            primaryStage.getScene().setRoot(newRoot);
        }
    }

    @FXML
    protected void onRitiroButtonClick() throws IOException {
        Window owner = ritiroButton.getScene().getWindow();
        String s = UserSession.getUserName();
        String motivo="",stato="";
        boolean b=false;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM richieste where codiceFiscaleRichiedente=?");
            stmt.setString(1, s);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                motivo = rs.getString("motivoRichiesta");
                stato=rs.getString("statoRichiesta");
                if(stato.equals("aperta") && motivo.equals("Ritiro passaporto")){
                    b=true;
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);;
        }
        if(b){
            Stage primaryStage = (Stage) Main.getScene().getWindow();
            Parent newRoot = FXMLLoader.load(Main.class.getResource("views/citizen/disponibilityCitizen.fxml"));
            primaryStage.getScene().setRoot(newRoot);
        }
        else{
            showAlert(Alert.AlertType.ERROR,owner, "Errore!", "Prima di richiedere il ritiro del passaporto è necessario fare richiesta di rilascio e aspettare l'avviso di possibile ritiro.");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String s = UserSession.getUserName();

        //GESTIONE MESSAGGIO BENVENUTO
        String genere = "F";
        String nome="";
        String cognome="";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            PreparedStatement stmt = conn.prepareStatement("SELECT nome, cognome, sesso FROM anagrafiche where codiceFiscale=?");
            stmt.setString(1, UserSession.getUserName());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                genere = rs.getString("sesso");
                nome = rs.getString("nome");
                cognome = rs.getString("cognome");
            }
            if (genere.equals("M")) {
                welcomeLabel.setText("Benvenuto, " + nome +" "+cognome);
            } else {
                welcomeLabel.setText("Benvenuta, " + nome +" "+cognome);
            }
        }
        catch (Exception e) {
            System.out.println(e);;
        }

        //GESTIONE APPUNTAMENTI
        String motivo="";
        Date data=null;
        int idSede=0;
        String nomeSede="";
        String indirizzo="";
        String comune="";
        String stato="";
        int civicoSede=0;
        String res="";
        String res2="";
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM richieste where codiceFiscaleRichiedente=?");
            stmt.setString(1, s);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                motivo = rs.getString("motivoRichiesta");
                data = rs.getDate("dataAppuntamento");
                idSede = rs.getInt("idSedeAppuntamento");
                stato=rs.getString("statoRichiesta");
            }
            stmt = conn.prepareStatement("SELECT * FROM sedi where idSede=?");
            stmt.setInt(1, idSede);
            rs = stmt.executeQuery();
            while (rs.next()) {
                nomeSede = rs.getString("nomeSede");
                indirizzo = rs.getString("indirizzoSede");
                civicoSede=rs.getInt("numeroCivicoSede");
                comune=rs.getString("comuneSede");
            }
            if(motivo.equals("Ritiro passaporto") && data!=null && !stato.equals("chiusa")){
                res=" Ritiro passaporto, sede di "+comune+", "+indirizzo+" "+civicoSede+"\n "+data;
                res2=" Ricordati di portare con te la ricevuta!";
            }
            else if ( !(motivo.equals("Ritiro passaporto")) && data!=null && !stato.equals("chiusa")){
                res = " Richiesta passaporto, sede di " + comune + ", " + indirizzo + " " + civicoSede + "\n " + data;
                res2 = " # il modulo di richiesta del passaporto stampato e debitamente compilato. \n # l’informativa sul trattamento dei dati personali. \n # un documento di riconoscimento valido e una fotocopia del documento. \n # 2 foto (si possono accettare SOLO ed ESCLUSIVAMENTE fotografie conformi alle norme ICAO). \n # la ricevuta del pagamento a mezzo c/c di € 42.50 per il passaporto ordinario. \n # un contrassegno amministrativo da € 73,50. \n # la denuncia in caso di furto o smarrimento.";
            }
            else{
                res=" Nessun appuntamento previsto prossimamente";
                res2=res;
            }
            Label l1 = new Label();
            l1.setStyle("-fx-font-size: 20px; -fx-wrap-text: true;");
            l1.setText(res);
            tabAppuntamenti.setContent(l1);
            tabAppuntamenti.setStyle("-fx-font-size: 20px;");
            Label l2 = new Label();
            l2.setStyle("-fx-font-size: 20px; -fx-wrap-text: true;");
            l2.setText(res2);
            tabDocumenti.setContent(l2);
            tabDocumenti.setStyle("-fx-font-size: 20px;");
        }
        catch (Exception e) {
            System.out.println(e);;
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
}
