package it.roombooking.booking.repository;

import it.roombooking.booking.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtenteRepo extends JpaRepository<Utente, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    Optional<Utente> findByEmailIgnoreCase(String email);
    Optional<Utente> findByNameIgnoreCase(String name);
}
