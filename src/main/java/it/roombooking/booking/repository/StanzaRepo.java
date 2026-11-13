package it.roombooking.booking.repository;

import it.roombooking.booking.model.Stanza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StanzaRepo extends JpaRepository<Stanza, Long> {
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    List<Stanza> findByCapienzaGreaterThanEqualOrderByCapienzaAsc(Integer capienzaMinima);

    @Query("SELECT s FROM Stanza s WHERE (:capienzaMinima IS NULL OR s.capienza >= :capienzaMinima) " +
           "AND NOT EXISTS (" +
           "  SELECT p FROM Prenotazione p WHERE p.stanza = s " +
           "  AND (p.dataI < :endDate AND p.dataF > :startDate)" +
           ") ORDER BY s.nome ASC")
    List<Stanza> findAvailableRooms(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    @Param("capienzaMinima") Integer capienzaMinima);
}
