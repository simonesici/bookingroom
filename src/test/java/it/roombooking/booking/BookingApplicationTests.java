package it.roombooking.booking;

import it.roombooking.booking.dto.PrenotazioneRequestDTO;
import it.roombooking.booking.dto.PrenotazioneResponseDTO;
import it.roombooking.booking.dto.StanzaDTO;
import it.roombooking.booking.dto.UtenteDTO;
import it.roombooking.booking.model.Utente;
import it.roombooking.booking.repository.UtenteRepo;
import it.roombooking.booking.service.PrenotazioneDAO;
import it.roombooking.booking.service.StanzaDAO;
import it.roombooking.booking.service.UtenteDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UtenteDAO utenteDAO;

	@Autowired
	private UtenteRepo utenteRepo;

	@Autowired
	private StanzaDAO stanzaDAO;

	@Autowired
	private PrenotazioneDAO prenotazioneDAO;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	public void contextLoads() {
	}

	@Test
	public void testCreateUtenteAuthAndPasswordProtection() throws Exception {
		String payload = "{\"name\":\"Mario\",\"email\":\"mario@example.com\",\"password\":\"segreta123\"}";

		// Without auth -> 401 Unauthorized
		mockMvc.perform(post("/api/utenti")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isUnauthorized());

		// With HTTP Basic auth -> 200 OK and password not exposed in JSON response
		String basicAuth = "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));
		mockMvc.perform(post("/api/utenti")
				.header(HttpHeaders.AUTHORIZATION, basicAuth)
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Mario"))
				.andExpect(jsonPath("$.email").value("mario@example.com"))
				.andExpect(jsonPath("$.password").doesNotExist());

		// Verify password in DB is BCrypt encoded
		Utente saved = utenteRepo.findAll().stream()
				.filter(u -> "mario@example.com".equals(u.getEmail()))
				.findFirst()
				.orElseThrow();
		assertNotEquals("segreta123", saved.getPassword());
		assertTrue(passwordEncoder.matches("segreta123", saved.getPassword()));
	}

	@Test
	public void testBookingDateValidationAndAdjacentSlots() {
		UtenteDTO u = new UtenteDTO();
		u.setName("Luigi");
		u.setEmail("luigi@example.com");
		u.setPassword("pass");
		u = utenteDAO.save(u);

		StanzaDTO s = new StanzaDTO();
		s.setNome("Sala A");
		s = stanzaDAO.save(s);

		// Invalid dates (start >= end)
		PrenotazioneRequestDTO invalidReq = new PrenotazioneRequestDTO();
		invalidReq.setIdUtente(u.getId());
		invalidReq.setIdStanza(s.getId());
		invalidReq.setDataI(LocalDateTime.of(2026, 5, 10, 11, 0));
		invalidReq.setDataF(LocalDateTime.of(2026, 5, 10, 10, 0));
		assertThrows(RuntimeException.class, () -> prenotazioneDAO.save(invalidReq));

		// First valid booking: 09:00 -> 10:00
		PrenotazioneRequestDTO firstReq = new PrenotazioneRequestDTO();
		firstReq.setIdUtente(u.getId());
		firstReq.setIdStanza(s.getId());
		firstReq.setDataI(LocalDateTime.of(2026, 5, 10, 9, 0));
		firstReq.setDataF(LocalDateTime.of(2026, 5, 10, 10, 0));
		PrenotazioneResponseDTO firstSaved = prenotazioneDAO.save(firstReq);
		assertNotNull(firstSaved.getId());

		// Adjacent booking: 10:00 -> 11:00 should succeed
		PrenotazioneRequestDTO adjacentReq = new PrenotazioneRequestDTO();
		adjacentReq.setIdUtente(u.getId());
		adjacentReq.setIdStanza(s.getId());
		adjacentReq.setDataI(LocalDateTime.of(2026, 5, 10, 10, 0));
		adjacentReq.setDataF(LocalDateTime.of(2026, 5, 10, 11, 0));
		PrenotazioneResponseDTO adjacentSaved = prenotazioneDAO.save(adjacentReq);
		assertNotNull(adjacentSaved.getId());

		// Overlapping booking: 09:30 -> 10:30 should fail
		PrenotazioneRequestDTO overlapReq = new PrenotazioneRequestDTO();
		overlapReq.setIdUtente(u.getId());
		overlapReq.setIdStanza(s.getId());
		overlapReq.setDataI(LocalDateTime.of(2026, 5, 10, 9, 30));
		overlapReq.setDataF(LocalDateTime.of(2026, 5, 10, 10, 30));
		assertThrows(RuntimeException.class, () -> prenotazioneDAO.save(overlapReq));
	}

	@Test
	public void testUniquenessExceptionHandlingAndTimelineByYearMonth() throws Exception {
		String basicAuth = "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));

		// 1. Create unique user and verify duplicate email returns 409 Conflict
		String userPayload = "{\"name\":\"Anna\",\"email\":\"anna@example.com\",\"password\":\"pass123\"}";
		mockMvc.perform(post("/api/utenti")
				.header(HttpHeaders.AUTHORIZATION, basicAuth)
				.contentType(MediaType.APPLICATION_JSON)
				.content(userPayload))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/utenti")
				.header(HttpHeaders.AUTHORIZATION, basicAuth)
				.contentType(MediaType.APPLICATION_JSON)
				.content(userPayload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));

		// 2. Create unique room and verify duplicate name returns 409 Conflict
		String roomPayload = "{\"nome\":\"Sala Plenaria\"}";
		mockMvc.perform(post("/api/stanze")
				.contentType(MediaType.APPLICATION_JSON)
				.content(roomPayload))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/stanze")
				.contentType(MediaType.APPLICATION_JSON)
				.content(roomPayload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));

		// 3. Booking with nonexistent user -> 404 Not Found
		mockMvc.perform(post("/api/prenotazioni")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"idUtente\":99999,\"idStanza\":1,\"dataI\":\"2027-06-15T09:00:00\",\"dataF\":\"2027-06-15T10:00:00\"}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));

		// 4. Timeline filter by year and month
		UtenteDTO u = new UtenteDTO();
		u.setName("Paolo");
		u.setEmail("paolo@example.com");
		u.setPassword("pass");
		u = utenteDAO.save(u);

		StanzaDTO s = new StanzaDTO();
		s.setNome("Sala Timeline");
		s = stanzaDAO.save(s);

		PrenotazioneRequestDTO b2026 = new PrenotazioneRequestDTO();
		b2026.setIdUtente(u.getId());
		b2026.setIdStanza(s.getId());
		b2026.setDataI(LocalDateTime.of(2026, 11, 10, 9, 0));
		b2026.setDataF(LocalDateTime.of(2026, 11, 10, 10, 0));
		prenotazioneDAO.save(b2026);

		PrenotazioneRequestDTO b2027 = new PrenotazioneRequestDTO();
		b2027.setIdUtente(u.getId());
		b2027.setIdStanza(s.getId());
		b2027.setDataI(LocalDateTime.of(2027, 11, 10, 9, 0));
		b2027.setDataF(LocalDateTime.of(2027, 11, 10, 10, 0));
		prenotazioneDAO.save(b2027);

		mockMvc.perform(get("/api/prenotazioni/timeline/2026/11"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		mockMvc.perform(get("/api/prenotazioni/timeline/2027/11"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		// Invalid month -> 400 Bad Request
		mockMvc.perform(get("/api/prenotazioni/timeline/2026/13"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}
}
