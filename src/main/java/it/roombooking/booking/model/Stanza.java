package it.roombooking.booking.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
public class Stanza implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private Integer capienza;

    private String descrizione;

    public Stanza() {}

    public Stanza(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Stanza(Long id, String nome, Integer capienza, String descrizione) {
        this.id = id;
        this.nome = nome;
        this.capienza = capienza;
        this.descrizione = descrizione;
    }

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
