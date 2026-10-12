package it.roombooking.booking.controller;

import it.roombooking.booking.dto.StanzaDTO;
import it.roombooking.booking.service.StanzaDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/stanze")
public class StanzaCtrl {

    @Autowired
    private StanzaDAO stanzaDAO;

    @PostMapping
    public ResponseEntity<StanzaDTO> createRoom(@Valid @RequestBody StanzaDTO room) {
        return ResponseEntity.ok(stanzaDAO.save(room));
    }

    @GetMapping
    public ResponseEntity<List<StanzaDTO>> getAllRoom() {
        return ResponseEntity.ok(stanzaDAO.getAllRoom());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StanzaDTO> getByID(@PathVariable("id") Long id) {
        StanzaDTO dto = stanzaDAO.getById(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StanzaDTO> updateRoom(@PathVariable("id") Long id, @Valid @RequestBody StanzaDTO roomDtl) {
        StanzaDTO updated = stanzaDAO.update(id, roomDtl);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("id") Long id) {
        if (stanzaDAO.deleteRoom(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
