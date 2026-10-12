package it.roombooking.booking.repository;

import it.roombooking.booking.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtenteRepo extends JpaRepository<Utente, Long> {
}
