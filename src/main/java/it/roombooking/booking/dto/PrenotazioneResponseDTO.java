package it.roombooking.booking.dto;

import java.time.LocalDateTime;

public class PrenotazioneResponseDTO {
    private Long id;
    private LocalDateTime dataCreation;
    private UtenteDTO utente;
    private StanzaDTO stanza;
    private LocalDateTime dataI;
    private LocalDateTime dataF;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataCreation() {
        return dataCreation;
    }

    public void setDataCreation(LocalDateTime dataCreation) {
        this.dataCreation = dataCreation;
    }

    public UtenteDTO getUtente() {
        return utente;
    }

    public void setUtente(UtenteDTO utente) {
        this.utente = utente;
    }

    public StanzaDTO getStanza() {
        return stanza;
    }

    public void setStanza(StanzaDTO stanza) {
        this.stanza = stanza;
    }

    public LocalDateTime getDataI() {
        return dataI;
    }

    public void setDataI(LocalDateTime dataI) {
        this.dataI = dataI;
    }

    public LocalDateTime getDataF() {
        return dataF;
    }

    public void setDataF(LocalDateTime dataF) {
        this.dataF = dataF;
    }
}
