package it.roombooking.booking.controller;

import it.roombooking.booking.dto.PrenotazioneRequestDTO;
import it.roombooking.booking.dto.PrenotazioneResponseDTO;
import it.roombooking.booking.service.PrenotazioneDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/prenotazioni")
public class PrenotazioneCtrl {

    @Autowired
    private PrenotazioneDAO prenotazioneDAO;

    @PostMapping
    public ResponseEntity<PrenotazioneResponseDTO> createBooking(@Valid @RequestBody PrenotazioneRequestDTO request) {
        PrenotazioneResponseDTO response = prenotazioneDAO.save(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PrenotazioneResponseDTO>> getAllBooking() {
        return ResponseEntity.ok(prenotazioneDAO.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrenotazioneResponseDTO> getBookingById(@PathVariable("id") Long id) {
        PrenotazioneResponseDTO dto = prenotazioneDAO.findOne(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrenotazioneResponseDTO> updateBooking(@PathVariable("id") Long id, @Valid @RequestBody PrenotazioneRequestDTO request) {
        PrenotazioneResponseDTO updated = prenotazioneDAO.update(id, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable("id") Long id) {
        if (prenotazioneDAO.delete(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/timeline/{mese}")
    public ResponseEntity<List<PrenotazioneResponseDTO>> getTimelineByMonth(@PathVariable("mese") int mese) {
        return ResponseEntity.ok(prenotazioneDAO.findByMonth(mese));
    }

    @GetMapping("/timeline/{anno}/{mese}")
    public ResponseEntity<List<PrenotazioneResponseDTO>> getTimelineByYearAndMonth(
            @PathVariable("anno") int anno,
            @PathVariable("mese") int mese) {
        return ResponseEntity.ok(prenotazioneDAO.findByYearAndMonth(anno, mese));
    }
}
