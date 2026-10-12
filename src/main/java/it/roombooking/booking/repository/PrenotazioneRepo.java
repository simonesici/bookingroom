package it.roombooking.booking.repository;

import it.roombooking.booking.model.Prenotazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PrenotazioneRepo extends JpaRepository<Prenotazione, Long> {

    @Query("SELECT p FROM Prenotazione p WHERE EXTRACT(MONTH FROM p.dataI) = :month")
    List<Prenotazione> findByMonth(@Param("month") int month);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM Prenotazione p WHERE p.stanza.id = :stanzaId " +
           "AND p.id != :excludeId " + // Useful for updates
           "AND ((p.dataI <= :endDate AND p.dataF >= :startDate))")
    boolean isStanzaOccupied(@Param("stanzaId") Long stanzaId,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate,
                             @Param("excludeId") Long excludeId);
}
