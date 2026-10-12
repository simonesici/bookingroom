package it.roombooking.booking.dto;

import jakarta.validation.constraints.NotBlank;

public class StanzaDTO {
    private Long id;

    @NotBlank(message = "Il nome della stanza non può essere vuoto")
    private String nome;

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
}
