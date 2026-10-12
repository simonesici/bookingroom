package it.roombooking.booking.controller;

import it.roombooking.booking.dto.UtenteDTO;
import it.roombooking.booking.service.UtenteDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/utenti")
public class UtenteCtrl {

    @Autowired
    private UtenteDAO utenteDAO;

    @PostMapping
    public ResponseEntity<UtenteDTO> createUtente(@Valid @RequestBody UtenteDTO utenteDTO) {
        return ResponseEntity.ok(utenteDAO.save(utenteDTO));
    }

    @GetMapping
    public ResponseEntity<List<UtenteDTO>> getAllUtente() {
        return ResponseEntity.ok(utenteDAO.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtenteDTO> getUtenteById(@PathVariable("id") Long id) {
        UtenteDTO dto = utenteDAO.findOne(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtenteDTO> updateUtente(@PathVariable("id") Long id, @Valid @RequestBody UtenteDTO utDetails) {
        UtenteDTO updated = utenteDAO.update(id, utDetails);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUtente(@PathVariable("id") Long id) {
        if (utenteDAO.delete(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
