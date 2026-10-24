package it.roombooking.booking.service;

import it.roombooking.booking.dto.UtenteDTO;
import it.roombooking.booking.model.Utente;
import it.roombooking.booking.repository.UtenteRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UtenteDAO {

    @Autowired
    private UtenteRepo utenteRepo;

    public UtenteDTO save(UtenteDTO utenteDTO) {
        Utente utente = new Utente();
        utente.setName(utenteDTO.getName());
        utente.setEmail(utenteDTO.getEmail());
        utente.setPassword(utenteDTO.getPassword());

        Utente saved = utenteRepo.save(utente);
        return mapToDTO(saved);
    }

    public List<UtenteDTO> findAll() {
        return utenteRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UtenteDTO findOne(Long id) {
        return utenteRepo.findById(id).map(this::mapToDTO).orElse(null);
    }

    public UtenteDTO update(Long id, UtenteDTO utenteDTO) {
        return utenteRepo.findById(id).map(utente -> {
            utente.setName(utenteDTO.getName());
            utente.setEmail(utenteDTO.getEmail());
            utente.setPassword(utenteDTO.getPassword());
            return mapToDTO(utenteRepo.save(utente));
        }).orElse(null);
    }

    public boolean delete(Long id) {
        if (utenteRepo.existsById(id)) {
            utenteRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public UtenteDTO mapToDTO(Utente utente) {
        UtenteDTO dto = new UtenteDTO();
        dto.setId(utente.getId());
        dto.setName(utente.getName());
        dto.setEmail(utente.getEmail());
        dto.setPassword(utente.getPassword());
        return dto;
    }
}
