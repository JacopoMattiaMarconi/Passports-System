package it.univr.passaporti.administration;

import it.univr.passaporti.Main;
import it.univr.passaporti.generic.Richiesta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Date;
import java.util.ResourceBundle;

public class RequestsControllerAdministration implements Initializable {
    private final static String urldb = "jdbc:mysql://localhost:3306/passaporti";
    private final static String userdb = "root";
    private final static String passdb = "";

    @FXML
    protected TableView tabellaRichieste;
    private Connection conn = null;
    @FXML
    private CheckBox oldRequestsCheckBox;

    @FXML
    protected void update() {
        while (!tabellaRichieste.getItems().isEmpty())
            tabellaRichieste.getItems().remove(0);
        createTable();
    }

    @FXML
    protected void onLogoutClick() throws IOException {
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

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createTable();
    }

    private void createTable() {
        ObservableList<TableColumn> columns = tabellaRichieste.getColumns();
        String[] columnsProperty = {"nome", "cognome", "codfisc", "dataAppuntamento", "dataRichiesta", "motivazione", "stato"};
        int i = 0;
        for (TableColumn c : columns) {
            c.setCellValueFactory(new PropertyValueFactory<>(columnsProperty[i]));
            i++;
        }

        ObservableList<Richiesta> values = FXCollections.observableArrayList();
        String nome = "", cognome = "", codfisc = "", motivo = "", stato = "";
        Date dataAppuntamento;
        Date dataRichiesta;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            Statement stmt = conn.createStatement();

            String query = "SELECT * FROM richieste r JOIN cittadini c ON (r.codiceFiscaleRichiedente = c.codiceFiscale)";
            if (!oldRequestsCheckBox.isSelected())
                query += " WHERE r.statoRichiesta != 'chiusa'";

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                ChoiceBox box = new ChoiceBox();
                String[] statiRichieste = {"aperta", "in elaborazione", "pronta", "chiusa"};
                for (String s : statiRichieste)
                    box.getItems().add(s);

                nome = rs.getString("nome");
                cognome = rs.getString("cognome");
                codfisc = rs.getString("codiceFiscale");
                motivo = rs.getString("motivoRichiesta");
                stato = rs.getString("statoRichiesta");
                dataRichiesta = rs.getDate("dataRichiesta");
                dataAppuntamento = rs.getDate("dataAppuntamento");

                box.setValue(stato);
                box.setId(codfisc + "," + dataRichiesta);
                box.setOnAction(e -> changeRequestState(box));

                values.add(new Richiesta(nome, cognome, codfisc, dataAppuntamento, dataRichiesta, motivo, box));

                tabellaRichieste.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                tabellaRichieste.setItems(values);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @FXML
    private void changeRequestState(ChoiceBox state) {
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        Date lastDate=null;
        String motivo="";
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(urldb, userdb, passdb);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM richieste WHERE codiceFiscaleRichiedente = ? ORDER BY dataRichiesta");
            stmt.setString(1, state.getId().substring(0,16));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                motivo = rs.getString("motivoRichiesta");
                lastDate = rs.getDate("dataRichiesta");
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        if(lastDate.compareTo(today) != 0) {
            try {
                PreparedStatement stmt = conn.prepareStatement("UPDATE richieste SET statoRichiesta = ? WHERE codiceFiscaleRichiedente = ? AND dataRichiesta = ? AND statoRichiesta != 'chiusa' AND dataAppuntamento IS NOT NULL");
                stmt.setString(1, (String) state.getValue());
                stmt.setString(2, state.getId().substring(0,16));
                stmt.setDate(3, (java.sql.Date) lastDate);
                stmt.executeUpdate();
            } catch (SQLException e) {
            }

            if ( !(motivo.equals("Ritiro passaporto")) && state.getValue().equals("chiusa")) {
                try {
                    String query = "INSERT INTO richieste (codiceFiscaleRichiedente, dataRichiesta, motivoRichiesta, statoRichiesta) VALUES(?,?,?,?)";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, state.getId().substring(0,16));
                    stmt.setDate(2, today);
                    stmt.setString(3, "Ritiro passaporto");
                    stmt.setString(4, "aperta");
                    stmt.execute();
                } catch (SQLException e) {
                }
            }
            update();
        }
    }
}