package it.roombooking.booking.repository;

import it.roombooking.booking.model.Stanza;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StanzaRepo extends JpaRepository<Stanza, Long> {
}
