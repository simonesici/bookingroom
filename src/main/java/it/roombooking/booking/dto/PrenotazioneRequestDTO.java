package it.roombooking.booking.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class PrenotazioneRequestDTO {

    @NotNull(message = "L'ID dell'utente è obbligatorio")
    private Long idUtente;

    @NotNull(message = "L'ID della stanza è obbligatorio")
    private Long idStanza;

    @NotNull(message = "La data di inizio è obbligatoria")
    private LocalDateTime dataI;

    @NotNull(message = "La data di fine è obbligatoria")
    private LocalDateTime dataF;

    public Long getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(Long idUtente) {
        this.idUtente = idUtente;
    }

    public Long getIdStanza() {
        return idStanza;
    }

    public void setIdStanza(Long idStanza) {
        this.idStanza = idStanza;
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
