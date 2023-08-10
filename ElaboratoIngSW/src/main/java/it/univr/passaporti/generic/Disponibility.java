package it.univr.passaporti.generic;

public class Disponibility {
    private String oraInizio;
    private String oraFine;
    private String tipologia;
    private int numeroSlot;

    public Disponibility(String oraInizio, String oraFine, String tipologia, int numeroSlot){
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
        this.tipologia = tipologia;
        this.numeroSlot = numeroSlot;
    }

    public String getOraInizio() {
        return oraInizio;
    }

    public void setOraInizio(String oraInizio) {
        this.oraInizio = oraInizio;
    }

    public String getOraFine() {
        return oraFine;
    }

    public void setOraFine(String oraFine) {
        this.oraFine = oraFine;
    }

    public String getTipologia() {
        return tipologia;
    }

    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    public int getNumeroSlot() {
        return numeroSlot;
    }

    public void setNumeroSlot(int numeroSlot) {
        this.numeroSlot = numeroSlot;
    }
}
