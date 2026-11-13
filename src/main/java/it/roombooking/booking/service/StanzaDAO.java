package it.roombooking.booking.service;

import it.roombooking.booking.dto.StanzaDTO;
import it.roombooking.booking.exception.BadRequestException;
import it.roombooking.booking.exception.ResourceConflictException;
import it.roombooking.booking.model.Stanza;
import it.roombooking.booking.repository.StanzaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StanzaDAO {

    @Autowired
    private StanzaRepo stanzaRepo;

    @Transactional
    public StanzaDTO save(StanzaDTO dto) {
        if (stanzaRepo.existsByNomeIgnoreCase(dto.getNome())) {
            throw new ResourceConflictException("Esiste già una stanza con nome: " + dto.getNome());
        }
        Stanza room = new Stanza();
        room.setNome(dto.getNome());
        room.setCapienza(dto.getCapienza());
        room.setDescrizione(dto.getDescrizione());
        return mapToDTO(stanzaRepo.save(room));
    }

    @Transactional(readOnly = true)
    public List<StanzaDTO> getAllRoom() {
        return getAllRoom(null);
    }

    @Transactional(readOnly = true)
    public List<StanzaDTO> getAllRoom(Integer capienzaMinima) {
        if (capienzaMinima != null && capienzaMinima < 1) {
            throw new BadRequestException("La capienza minima deve essere almeno 1");
        }
        List<Stanza> stanze = (capienzaMinima != null)
                ? stanzaRepo.findByCapienzaGreaterThanEqualOrderByCapienzaAsc(capienzaMinima)
                : stanzaRepo.findAll();
        return stanze.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StanzaDTO> findAvailableRooms(LocalDateTime dataI, LocalDateTime dataF, Integer capienzaMinima) {
        if (dataI == null || dataF == null || !dataI.isBefore(dataF)) {
            throw new BadRequestException("La data di inizio deve essere antecedente alla data di fine");
        }
        if (capienzaMinima != null && capienzaMinima < 1) {
            throw new BadRequestException("La capienza minima deve essere almeno 1");
        }
        return stanzaRepo.findAvailableRooms(dataI, dataF, capienzaMinima).stream()
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
            if (stanzaRepo.existsByNomeIgnoreCaseAndIdNot(dto.getNome(), id)) {
                throw new ResourceConflictException("Esiste già una stanza con nome: " + dto.getNome());
            }
            room.setNome(dto.getNome());
            room.setCapienza(dto.getCapienza());
            room.setDescrizione(dto.getDescrizione());
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
        dto.setCapienza(stanza.getCapienza());
        dto.setDescrizione(stanza.getDescrizione());
        return dto;
    }
}
