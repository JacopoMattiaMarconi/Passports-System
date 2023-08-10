package it.univr.passaporti.generic;

import javafx.scene.control.ChoiceBox;

import java.util.Date;

public class Richiesta {
    private String nome;
    private String cognome;
    private String codfisc;
    private Date dataAppuntamento;
    private Date dataRichiesta;
    private String motivazione;
    private ChoiceBox stato;

    public Richiesta(String nome, String cognome, String codfisc, Date dataAppuntamento, Date dataRichiesta, String motivazione, ChoiceBox stato) {
        this.nome = nome;
        this.cognome = cognome;
        this.codfisc = codfisc;
        this.dataAppuntamento = dataAppuntamento;
        this.dataRichiesta = dataRichiesta;
        this.motivazione = motivazione;
        this.stato = stato;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getCodfisc() {
        return codfisc;
    }

    public void setCodfisc(String codfisc) {
        this.codfisc = codfisc;
    }

    public Date getDataAppuntamento() {
        return dataAppuntamento;
    }

    public void setDataAppuntamento(Date dataAppuntamento) {
        this.dataAppuntamento = dataAppuntamento;
    }

    public Date getDataRichiesta() {
        return dataRichiesta;
    }

    public void setDataRichiesta(Date dataRichiesta) {
        this.dataRichiesta = dataRichiesta;
    }

    public String getMotivazione() {
        return motivazione;
    }

    public void setMotivazione(String motivazione) {
        this.motivazione = motivazione;
    }

    public ChoiceBox getStato() {
        return stato;
    }

    public void setStato(ChoiceBox stato) {
        this.stato = stato;
    }
}
