package it.univr.passaporti.administration;

import it.univr.passaporti.Main;
import it.univr.passaporti.generic.Disponibility;
import it.univr.passaporti.generic.Sede;
import it.univr.passaporti.generic.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.*;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

public class DisponibilityControllerAdministration implements Initializable {
    private Connection conn = null;
    private static String urldb = "jdbc:mysql://localhost:3306/passaporti";
    private static String userdb = "root";
    private static String passdb = "";

    private Button selectedDateButton = null;

    ZonedDateTime dateFocus;
    ZonedDateTime today;

    @FXML
    private HBox monthYearSelector;

    @FXML
    private HBox daysLabels;

    @FXML
    private Text year;

    @FXML
    private Text month;

    @FXML
    private GridPane calendar;

    @FXML
    private ComboBox officeComboBox;

    @FXML
    private ComboBox causeComboBox;

    @FXML
    private ComboBox timeComboBox;

    @FXML
    private ComboBox slotNumberComboBox;

    @FXML
    private Button confirmButton;


    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT idSede FROM sedi");
            while (rs.next()) {
                Sede office = new Sede(rs.getInt("idSede"));
                officeComboBox.getItems().add(office);
            }
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        dateFocus = ZonedDateTime.now();
        today = ZonedDateTime.now();
        selectCause();
        selectTime();
        selectNumberOf();
    }


    private void selectDate(Button button){
        if(selectedDateButton != null) {
            Button oldSelectedDateButton = selectedDateButton;
            oldSelectedDateButton.setStyle(oldSelectedDateButton.getStyle() + "; -fx-border-color: #000000; -fx-border-width: 1px");
            oldSelectedDateButton.setOnMouseClicked(e -> selectDate(oldSelectedDateButton));
        }
        selectedDateButton = button;
        selectedDateButton.setStyle(selectedDateButton.getStyle() + "; -fx-border-color: #325B8D; -fx-border-width: 10px");
        selectedDateButton.setOnMouseClicked(e -> showDisponibility());
    }

    private void showDisponibility(){
        Date selectedDate = new Date(dateFocus.getYear() - 1900, dateFocus.getMonthValue() - 1, Integer.parseInt(selectedDateButton.getText()));
        int idSede = ((Sede)officeComboBox.getValue()).getId();

        ObservableList<Disponibility> values = FXCollections.observableArrayList();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*), oraInizio, oraFine, tipologia FROM disponibilita WHERE dataDisponibilita = ? AND idSede = ? AND codiceFiscaleCittadino IS NULL GROUP BY oraInizio, tipologia ORDER BY oraInizio");
            stmt.setDate(1, selectedDate);
            stmt.setInt(2, idSede);
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                values.add(new Disponibility(rs.getString("oraInizio"), rs.getString("oraFine"), rs.getString("tipologia"), rs.getInt("COUNT(*)")));

            }

            showPopup(selectedDate, values);
        } catch (SQLException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }
    }

    private void showPopup(Date selectedDate, ObservableList<Disponibility> values){
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        double width = 500;
        double height = 300;
        double x = stage.getWidth()/2 ;
        double y = stage.getHeight()/2 ;
        StackPane root = new StackPane();


        Stage popupStage = new Stage();
        popupStage.setTitle(selectedDate.toString());
        Scene scene = new Scene(root, width, height);
        popupStage.initModality(Modality.WINDOW_MODAL);

        TableView disponibilityTable = new TableView<>();
        disponibilityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        String[] columnsNames = {"Ora Inizio", "Ora Fine", "Tipologia", "Numero Slot"};
        String[] columnsProperty = {"oraInizio", "oraFine", "tipologia", "numeroSlot"};
        for (int i = 0; i < columnsProperty.length; i++) {
            TableColumn c = new TableColumn<>(columnsNames[i]);
            c.setCellValueFactory(new PropertyValueFactory<>(columnsProperty[i]));
            disponibilityTable.getColumns().add(c);
        }
        disponibilityTable.setItems(values);
        root.getChildren().add(disponibilityTable);
        popupStage.setScene(scene);
        popupStage.show();
        popupStage.setX(x);
        popupStage.setY(y);
    }

    public void selectCause(){
        causeComboBox.getItems().add("Richiesta passaporto");
        causeComboBox.getItems().add("Ritiro passaporto");
    }

    public void selectNumberOf(){
        for(int i=1; i<=10; i++){
            slotNumberComboBox.getItems().add(i);
        }
    }

    public void selectTime(){
        timeComboBox.getItems().add("08:00 - 09:00"); timeComboBox.getItems().add("09:00 - 10:00"); timeComboBox.getItems().add("10:00 - 11:00");
        timeComboBox.getItems().add("11:00 - 12:00"); timeComboBox.getItems().add("14:00 - 15:00"); timeComboBox.getItems().add("15:00 - 16:00");
        timeComboBox.getItems().add("16:00 - 17:00"); timeComboBox.getItems().add("17:00 - 18:00");
    }

    @FXML
    private void enableCalendar(){
        monthYearSelector.setVisible(true);
        daysLabels.setVisible(true);
        drawCalendar();
    }

    @FXML
    public void enableConfirmButton(){
        if(officeComboBox.getValue() == null || causeComboBox.getValue() == null || timeComboBox.getValue() == null || slotNumberComboBox.getValue() == null || selectedDateButton == null)
            confirmButton.setDisable(true);
        else
            confirmButton.setDisable(false);
    }

    @FXML
    public void onConfirmButtonClick(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            String[] infoTime = ((String)timeComboBox.getValue()).split("-");
            String time1=infoTime[0].substring(0,infoTime[0].length()-1).concat(":00");
            String time2=infoTime[1].substring(0,infoTime[1].length()-1).concat(":00");
            Sede office = (Sede) officeComboBox.getValue();
            Date selectedDate = new Date(dateFocus.getYear() - 1900, dateFocus.getMonthValue() - 1, Integer.parseInt(selectedDateButton.getText()));
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO disponibilita (dataDisponibilita, oraInizio, oraFine, idSede, tipologia) VALUES(?,?,?,?,?)");
            stmt.setDate(1, selectedDate);
            stmt.setString(2, time1);
            stmt.setString(3, time2);
            stmt.setInt(4, office.getId());
            stmt.setString(5, (String) causeComboBox.getValue());
            for(int i=0; i < (int)slotNumberComboBox.getValue();i++){
                stmt.execute();
            }

            showAlert(Alert.AlertType.INFORMATION, confirmButton.getScene().getWindow(), "Successo!", "Disponibilità aggiunte con successo!");
            stmt.close();
            conn.close();
        }
        catch (Exception e){
            System.out.println(e);
            showAlert(Alert.AlertType.ERROR, confirmButton.getScene().getWindow(), "Errore!", "Aggiunta delle disponibilità fallita.");
        }
    }

    @FXML
    void backOneMonth(ActionEvent event) {
        dateFocus = dateFocus.minusMonths(1);
        calendar.getChildren().clear();
        drawCalendar();
    }

    @FXML
    void forwardOneMonth(ActionEvent event) {
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
                button.setOnAction(e -> enableConfirmButton());


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

                        try {
                            Sede office = (Sede) officeComboBox.getValue();
                            java.sql.Date dateDisponibility = new Date(dateFocus.getYear() - 1900, dateFocus.getMonthValue() - 1, currentDate);
                            Connection conn = DriverManager.getConnection(urldb, userdb, passdb);
                            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM disponibilita WHERE idSede = ? AND dataDisponibilita = ? AND codiceFiscaleCittadino IS NULL");
                            stmt.setInt(1, office.getId());
                            stmt.setDate(2, dateDisponibility);
                            ResultSet rs = stmt.executeQuery();
                            rs.next();
                            if(rs.getInt(1) == 0) {
                                button.setStyle("-fx-background-color: #B0DEAC");
                            }else{
                                button.setStyle("-fx-background-color: #FFFEB7");
                            }
                            stmt.close();
                            conn.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        ZonedDateTime d = ZonedDateTime.of(dateFocus.getYear(), dateFocus.getMonthValue(), currentDate, today.getHour(), today.getMinute(), today.getSecond(), today.getNano(), dateFocus.getZone());
                        if(d.compareTo(today) <= 0) {
                            button.setStyle("-fx-background-color: #FF7C7C");
                            button.setDisable(true);
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
        Parent newRoot = FXMLLoader.load(Main.class.getResource("views/administration/homeAdministration.fxml"));
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
