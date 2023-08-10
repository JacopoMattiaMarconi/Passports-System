package it.univr.passaporti.generic;

import java.sql.*;

public class Sede {
    private int id;
    private String nome;
    private String comune;
    private String provincia;
    private String indirizzo;
    private String numeroCivico;
    private String telefono;
    private String cap;

    public Sede(int idSede){
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/passaporti", "root", "");
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM sedi WHERE idSede = ?");
            stmt.setInt(1, idSede);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                this.id = rs.getInt("idSede");
                this.nome = rs.getString("nomeSede");
                this.comune = rs.getString("comuneSede");
                this.provincia = rs.getString("provinciaSede");
                this.indirizzo = rs.getString("indirizzoSede");
                this.numeroCivico = rs.getString("numeroCivicoSede");
                this.telefono = rs.getString("telefono");
                this.cap = rs.getString("CAP");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public int getId(){
        return id;
    }

    public String toString(){
        return nome + ", " + indirizzo + " " + numeroCivico;
    }


}
