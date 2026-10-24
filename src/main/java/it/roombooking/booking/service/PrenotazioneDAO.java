package it.roombooking.booking.service;

import it.roombooking.booking.dto.PrenotazioneRequestDTO;
import it.roombooking.booking.dto.PrenotazioneResponseDTO;
import it.roombooking.booking.model.Prenotazione;
import it.roombooking.booking.model.Stanza;
import it.roombooking.booking.model.Utente;
import it.roombooking.booking.repository.PrenotazioneRepo;
import it.roombooking.booking.repository.StanzaRepo;
import it.roombooking.booking.repository.UtenteRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrenotazioneDAO {

    @Autowired
    private PrenotazioneRepo prenotazioneRepo;

    @Autowired
    private UtenteRepo utenteRepo;

    @Autowired
    private StanzaRepo stanzaRepo;

    @Autowired
    private UtenteDAO utenteDAO;

    @Autowired
    private StanzaDAO stanzaDAO;

    public PrenotazioneResponseDTO save(PrenotazioneRequestDTO dto) {
        if (prenotazioneRepo.isStanzaOccupied(dto.getIdStanza(), dto.getDataI(), dto.getDataF(), 0L)) {
            throw new RuntimeException("La stanza è già occupata in questo periodo");
        }

        Utente utente = utenteRepo.findById(dto.getIdUtente())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        Stanza stanza = stanzaRepo.findById(dto.getIdStanza())
                .orElseThrow(() -> new RuntimeException("Stanza non trovata"));

        Prenotazione prenotazione = new Prenotazione();
        prenotazione.setUtente(utente);
        prenotazione.setStanza(stanza);
        prenotazione.setDataI(dto.getDataI());
        prenotazione.setDataF(dto.getDataF());

        return mapToDTO(prenotazioneRepo.save(prenotazione));
    }

    public List<PrenotazioneResponseDTO> findAll() {
        return prenotazioneRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public PrenotazioneResponseDTO findOne(Long id) {
        return prenotazioneRepo.findById(id).map(this::mapToDTO).orElse(null);
    }

    public PrenotazioneResponseDTO update(Long id, PrenotazioneRequestDTO dto) {
        if (prenotazioneRepo.isStanzaOccupied(dto.getIdStanza(), dto.getDataI(), dto.getDataF(), id)) {
            throw new RuntimeException("La stanza è già occupata in questo periodo");
        }

        return prenotazioneRepo.findById(id).map(prenotazione -> {
            Utente utente = utenteRepo.findById(dto.getIdUtente())
                    .orElseThrow(() -> new RuntimeException("Utente non trovato"));
            Stanza stanza = stanzaRepo.findById(dto.getIdStanza())
                    .orElseThrow(() -> new RuntimeException("Stanza non trovata"));

            prenotazione.setUtente(utente);
            prenotazione.setStanza(stanza);
            prenotazione.setDataI(dto.getDataI());
            prenotazione.setDataF(dto.getDataF());
            return mapToDTO(prenotazioneRepo.save(prenotazione));
        }).orElse(null);
    }

    public boolean delete(Long id) {
        if (prenotazioneRepo.existsById(id)) {
            prenotazioneRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public List<PrenotazioneResponseDTO> findByMonth(int mese) {
        return prenotazioneRepo.findByMonth(mese).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PrenotazioneResponseDTO mapToDTO(Prenotazione prenotazione) {
        PrenotazioneResponseDTO dto = new PrenotazioneResponseDTO();
        dto.setId(prenotazione.getId());
        dto.setDataCreation(prenotazione.getDataCreation());
        dto.setDataI(prenotazione.getDataI());
        dto.setDataF(prenotazione.getDataF());

        if (prenotazione.getUtente() != null) {
            dto.setUtente(utenteDAO.mapToDTO(prenotazione.getUtente()));
        }

        if (prenotazione.getStanza() != null) {
            dto.setStanza(stanzaDAO.mapToDTO(prenotazione.getStanza()));
        }

        return dto;
    }
}
