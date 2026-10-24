package it.roombooking.booking.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
public class Prenotazione implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    private LocalDateTime dataCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utente", referencedColumnName = "id")
    private Utente utente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_stanza", referencedColumnName = "id")
    private Stanza stanza;

    private LocalDateTime dataI;
    private LocalDateTime dataF;

    public Prenotazione() {}

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

    public Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public Stanza getStanza() {
        return stanza;
    }

    public void setStanza(Stanza stanza) {
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
