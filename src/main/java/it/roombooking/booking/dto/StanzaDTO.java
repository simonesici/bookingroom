package it.roombooking.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class StanzaDTO {
    private Long id;

    @NotBlank(message = "Il nome della stanza non può essere vuoto")
    private String nome;

    @Min(value = 1, message = "La capienza deve essere almeno 1")
    private Integer capienza;

    private String descrizione;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getCapienza() {
        return capienza;
    }

    public void setCapienza(Integer capienza) {
        this.capienza = capienza;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
}
