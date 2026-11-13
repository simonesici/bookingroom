package it.roombooking.booking.service;

import it.roombooking.booking.dto.StanzaDTO;
import it.roombooking.booking.model.Stanza;
import it.roombooking.booking.repository.StanzaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StanzaDAO {

    @Autowired
    private StanzaRepo stanzaRepo;

    @Transactional
    public StanzaDTO save(StanzaDTO dto) {
        Stanza room = new Stanza();
        room.setNome(dto.getNome());
        return mapToDTO(stanzaRepo.save(room));
    }

    @Transactional(readOnly = true)
    public List<StanzaDTO> getAllRoom() {
        return stanzaRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StanzaDTO getById(Long id) {
        return stanzaRepo.findById(id).map(this::mapToDTO).orElse(null);
    }

    @Transactional
    public StanzaDTO update(Long id, StanzaDTO dto) {
        return stanzaRepo.findById(id).map(room -> {
            room.setNome(dto.getNome());
            return mapToDTO(stanzaRepo.save(room));
        }).orElse(null);
    }

    @Transactional
    public boolean deleteRoom(Long id) {
        if (stanzaRepo.existsById(id)) {
            stanzaRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public StanzaDTO mapToDTO(Stanza stanza) {
        StanzaDTO dto = new StanzaDTO();
        dto.setId(stanza.getId());
        dto.setNome(stanza.getNome());
        return dto;
    }
}
