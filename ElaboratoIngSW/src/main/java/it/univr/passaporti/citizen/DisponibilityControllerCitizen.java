package it.univr.passaporti.citizen;

import it.univr.passaporti.Main;
import it.univr.passaporti.generic.Sede;
import it.univr.passaporti.generic.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

public class DisponibilityControllerCitizen implements Initializable {
    private Connection conn = null;
    private static String urldb = "jdbc:mysql://localhost:3306/passaporti";
    private static String userdb = "root";
    private static String passdb = "";

    ZonedDateTime dateFocus;
    ZonedDateTime today;

    @FXML
    private Text year;

    @FXML
    private Text month;

    @FXML
    private GridPane calendar;

    @FXML
    private HBox monthYearSelector;

    @FXML
    private HBox daysLabels;

    @FXML
    private Label secondStep;

    @FXML
    private ComboBox officeComboBox;

    @FXML
    private ComboBox timeComboBox;

    @FXML
    private Label fourthStep;

    @FXML
    private Button confirmButton;

    private Button selectedDateButton;

    private String tipo;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            PreparedStatement stmt = conn.prepareStatement("SELECT idSede FROM sedi s WHERE (SELECT provinciaResidenza FROM anagrafiche a WHERE a.codiceFiscale=?)=s.provinciaSede ");
            stmt.setString(1, UserSession.getUserName());
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                Sede office = new Sede(rs.getInt("idSede"));
                officeComboBox.getItems().add(office);
            }


            //controllo che la disponibilità libera abbia la giusta tipologia di richiesta effettuata dal cittadino
            stmt = conn.prepareStatement("SELECT r.motivoRichiesta FROM richieste r WHERE codiceFiscaleRichiedente=? and r.statoRichiesta='aperta';  ");
            stmt.setString(1,UserSession.getUserName());
            rs = stmt.executeQuery();
            while(rs.next()){
                tipo = rs.getString("motivoRichiesta");
            }
            if(tipo.equals("Ritiro passaporto"))
                tipo="Ritiro passaporto";
            else
                tipo="Richiesta passaporto";

            stmt.close();
            conn.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }



    }

    @FXML
    private void enableCalendar(){
        timeComboBox.setVisible(false);
        fourthStep.setVisible(false);
        confirmButton.setDisable(true);
        secondStep.setVisible(true);
        monthYearSelector.setVisible(true);
        daysLabels.setVisible(true);
        dateFocus = ZonedDateTime.now();
        today = ZonedDateTime.now();
        drawCalendar();
    }

    private void selectDate(Button button){
        if(selectedDateButton != null) {
            selectedDateButton.setStyle(button.getStyle() + "; -fx-border-color: #000000");
            fourthStep.setVisible(false);
            confirmButton.setDisable(true);
        }

        selectedDateButton = button;
        selectedDateButton.setStyle(button.getStyle() + "; -fx-border-color: #F1FF24; -fx-border-width: 10px");
        timeComboBox.setVisible(true);
        selectTime();
    }

    //FUNZIONE PER MOSTRARE GLI ORARI DISPONIBILI
    public void selectTime(){
        timeComboBox.getItems().clear();
        timeComboBox.setPromptText("3. Seleziona orario");

        Date selectedDate = new Date(dateFocus.getYear() - 1900, dateFocus.getMonthValue() - 1, Integer.parseInt(selectedDateButton.getText()));
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(urldb, userdb, passdb);


            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT oraInizio, oraFine FROM disponibilita WHERE dataDisponibilita = ? AND idSede = ? and tipologia=? AND codiceFiscaleCittadino IS NULL ");
            stmt.setDate(1, selectedDate);
            Sede office = (Sede)officeComboBox.getValue();
            stmt.setInt(2, office.getId());
            stmt.setString(3, tipo);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                String oraInizio = rs.getString("oraInizio").substring(0,5);
                String oraFine = rs.getString("oraFine").substring(0,5);
                timeComboBox.getItems().add(oraInizio+ " - " + oraFine);
            }
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void enableBooking() {
        if(timeComboBox.getItems().isEmpty())
            return;

        if (!timeComboBox.getValue().equals("3. Seleziona orario")){
            fourthStep.setVisible(true);
            confirmButton.setDisable(false);
        }
    }

    @FXML
    void onConfirmButtonClick() throws IOException{
        Window owner = confirmButton.getScene().getWindow();
        Date selectedDate = new Date(dateFocus.getYear() - 1900, dateFocus.getMonthValue() - 1, Integer.parseInt(selectedDateButton.getText()));
        Sede office = (Sede) officeComboBox.getValue();
        String[] infoTime = ((String)timeComboBox.getValue()).split("-");
        String time=infoTime[0].substring(0,infoTime[0].length()-1).concat(":00");
        try {
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            PreparedStatement stmt = conn.prepareStatement("UPDATE disponibilita d SET d.codiceFiscaleCittadino=? WHERE idDisponibilita = (SELECT idDisponibilita FROM disponibilita WHERE d.dataDisponibilita=? and d.idSede=? and d.oraInizio=? AND tipologia = ? ORDER BY idDisponibilita LIMIT 1)");
            stmt.setString(1, UserSession.getUserName());
            stmt.setDate(2, selectedDate);
            stmt.setInt(3, office.getId());
            stmt.setString(4, time);
            stmt.setString(5, tipo);
            stmt.executeUpdate();

            stmt = conn.prepareStatement("UPDATE richieste r SET r.dataAppuntamento=?, r.idSedeAppuntamento=?, r.statoRichiesta='in elaborazione' WHERE r.codiceFiscaleRichiedente=? and statoRichiesta!='chiusa' and dataAppuntamento IS NULL and idSedeAppuntamento IS NULL");
            stmt.setDate(1, selectedDate);
            stmt.setInt(2, office.getId());
            stmt.setString(3, UserSession.getUserName());
            stmt.executeUpdate();
            conn.close();
            showAlert(Alert.AlertType.INFORMATION,owner, "Prenotazione confermata!", "Prenotazione eseguita con successo.");
            Stage primaryStage = (Stage) Main.getScene().getWindow();
            Parent newRoot = FXMLLoader.load(Main.class.getResource("views/citizen/homeCitizen.fxml"));
            primaryStage.getScene().setRoot(newRoot);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @FXML
    void backOneMonth() {
        selectedDateButton = null;
        timeComboBox.setVisible(false);
        fourthStep.setVisible(false);
        confirmButton.setDisable(true);
        dateFocus = dateFocus.minusMonths(1);
        calendar.getChildren().clear();
        drawCalendar();
    }

    @FXML
    void forwardOneMonth() {
        selectedDateButton = null;
        timeComboBox.setVisible(false);
        fourthStep.setVisible(false);
        confirmButton.setDisable(true);
        dateFocus = dateFocus.plusMonths(1);
        calendar.getChildren().clear();
        drawCalendar();
    }

    private void drawCalendar(){

        calendar.getChildren().clear();
        year.setText(String.valueOf(dateFocus.getYear()));
        month.setText(dateFocus.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN).toUpperCase());


        int monthMaxDate = dateFocus.getMonth().maxLength();

        //Se l'anno è bisestile
        if(dateFocus.getYear() % 4 != 0 && monthMaxDate == 29){
            monthMaxDate = 28;
        }
        int dateOffset = ZonedDateTime.of(dateFocus.getYear(), dateFocus.getMonthValue(), 1,0,0,0,0,dateFocus.getZone()).getDayOfWeek().getValue();

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {

                Button button = new Button();

                button.setPrefWidth(100);
                button.setPrefHeight(100);
                button.setAlignment(Pos.TOP_CENTER);
                button.setOnMouseClicked(e -> selectDate(button));

                double rectangleHeight = 100;

                int calculatedDate = (j+1)+(7*i);
                if(calculatedDate > dateOffset - 1){
                    int currentDate = calculatedDate - (dateOffset - 1);
                    if(currentDate <= monthMaxDate && currentDate > 0){
                        Text date = new Text(String.valueOf(currentDate));
                        double textTranslationY = - (rectangleHeight / 2) * 0.75;
                        date.setTranslateY(textTranslationY);
                        button.setText(String.valueOf(currentDate));
                        calendar.add(button, j, i);

                        ZonedDateTime d = ZonedDateTime.of(dateFocus.getYear(), dateFocus.getMonthValue(), currentDate, 0, 0, 0, 0, dateFocus.getZone());
                        if(d.compareTo(today) <= 0) {
                            button.setStyle("-fx-background-color: #FF7C7C");
                            button.setDisable(true);
                        }

                        try {
                            Sede office = (Sede) officeComboBox.getValue();
                            java.sql.Date dateDisponibility = new Date(dateFocus.getYear() - 1900, dateFocus.getMonthValue() - 1, currentDate);
                            Connection conn = DriverManager.getConnection(urldb, userdb, passdb);
                            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM disponibilita WHERE idSede = ? AND dataDisponibilita = ? AND tipologia = ? AND codiceFiscaleCittadino IS NULL");
                            stmt.setInt(1, office.getId());
                            stmt.setDate(2, dateDisponibility);
                            stmt.setString(3, tipo);
                            ResultSet rs = stmt.executeQuery();
                            rs.next();
                            if(rs.getInt(1) > 0) {
                                button.setStyle("-fx-background-color: #B0DEAC");
                            }else{
                                button.setStyle("-fx-background-color: #FF7C7C");
                                button.setDisable(true);
                            }
                            stmt.close();
                            conn.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        if(d.equals(today)){
                            button.setStyle(button.getStyle() + "; -fx-border-color: #0000FF;");
                        }else{
                            button.setStyle(button.getStyle() + "; -fx-border-color: #000000;");
                        }
                    }


                }

            }
        }
    }

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

    private static void showAlert(Alert.AlertType alertType, Window owner, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.show();
    }
}
