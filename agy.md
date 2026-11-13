# BookingRoom — Project Context & Handover (`agy.md`)

Documento di contesto per riprendere rapidamente lo sviluppo del progetto **bookingroom** in nuove sessioni.

---

## 1. Panoramica e Stack Tecnologico
Applicazione backend REST per la gestione di prenotazioni di stanze/sale riunioni.
- **Framework**: Spring Boot `3.2.5`
- **Linguaggio**: Java `21`
- **Build Tool**: Maven (`./mvnw`)
- **Database**:
  - Runtime/Sviluppo: **PostgreSQL** (`jdbc:postgresql://localhost:5432/booking_db`, utente/password: `postgres`/`postgres`)
  - Test: **H2** in-memory (`jdbc:h2:mem:testdb`)
- **ORM**: Spring Data JPA / Hibernate (`ddl-auto=update` in dev, `create-drop` in test)
- **Sicurezza**: Spring Security (HTTP Basic + `BCryptPasswordEncoder`)
- **Documentazione API**: SpringDoc OpenAPI `2.5.0` (`/swagger-ui/index.html`, `/v3/api-docs`)
- **Containerizzazione**: `Dockerfile` multi-stage (Java 21) e `docker-compose.yml` (PostgreSQL 16 + App).
- **Timezone**: `Europe/Rome` impostata di default all'avvio in `BookingApplication`.

---

## 2. Struttura del Progetto
Package base: `it.roombooking.booking` (`src/main/java/it/roombooking/booking/`)

- **`BookingApplication.java`**: Entry point Spring Boot con `@EnableJpaAuditing` e timezone `Europe/Rome`.
- **`config/WebSecConfig.java`**: Configurazione Spring Security:
  - CSRF disabilitato per utilizzo REST API.
  - Swagger UI pubblico.
  - `POST /api/utenti` protetto da autenticazione **HTTP Basic** (credenziali da `application.properties`: `admin` / `admin`).
  - Tutti gli altri endpoint attualmente pubblici (`permitAll()`).
  - Bean `PasswordEncoder` (`BCryptPasswordEncoder`) e `UserDetailsService` (`InMemoryUserDetailsManager`).
- **`model/`**:
  - `Utente`: `id`, `name`, `email` (`unique = true`), `password` (hash BCrypt).
  - `Stanza`: `id`, `nome` (`unique = true`), `capienza` (`Integer`), `descrizione` (`String`).
  - `Prenotazione`: `id`, `dataCreation` (`@CreationTimestamp`), `utente` (`@ManyToOne(LAZY)`), `stanza` (`@ManyToOne(LAZY)`), `dataI` (`LocalDateTime`), `dataF` (`LocalDateTime`).
- **`exception/`**:
  - `ResourceNotFoundException` (`404 Not Found`), `ResourceConflictException` (`409 Conflict`), `BadRequestException` (`400 Bad Request`).
  - `GlobalExceptionHandler` (`@RestControllerAdvice`): mappa centralmente le eccezioni di dominio, i vincoli DB (`DataIntegrityViolationException`) e gli errori di validazione (`MethodArgumentNotValidException`) in risposte JSON strutturate.
- **`dto/`**:
  - `UtenteDTO`: validazione `@NotBlank` / `@Email`; campo `password` annotato con `@JsonProperty(access = WRITE_ONLY)` per non esporlo mai nelle risposte JSON.
  - `StanzaDTO`: `id`, `nome` (`@NotBlank`), `capienza` (`@Min(1)`), `descrizione`.
  - `PrenotazioneRequestDTO`: `idUtente`, `idStanza`, `dataI`, `dataF` (tutti `@NotNull`).
  - `PrenotazioneResponseDTO`: `id`, `dataCreation`, `UtenteDTO`, `StanzaDTO`, `dataI`, `dataF`.
- **`repository/`**:
  - `UtenteRepo`: include check `existsByEmailIgnoreCase` / `existsByEmailIgnoreCaseAndIdNot`.
  - `StanzaRepo`: include check di unicità sul nome, filtro per capienza (`findByCapienzaGreaterThanEqualOrderByCapienzaAsc`) e ricerca stanze libere in un intervallo temporale (`findAvailableRooms`).
  - `PrenotazioneRepo`:
    - `findAll()`, `findById(Long id)`, `findByMonth(int month)`, `findByYearAndMonth(int year, int month)`, `findByStanzaIdOrderByDataIAsc(Long stanzaId)`, `findByUtenteIdOrderByDataIAsc(Long utenteId)` con `@EntityGraph(attributePaths = {"utente", "stanza"})`.
    - `isStanzaOccupied(stanzaId, startDate, endDate, excludeId)`: verifica sovrapposizioni con `p.dataI < :endDate AND p.dataF > :startDate` (consente prenotazioni adiacenti, es. `09:00-10:00` e `10:00-11:00`).
- **`service/`** (`UtenteDAO`, `StanzaDAO`, `PrenotazioneDAO`):
  - Tutti i metodi di scrittura sono `@Transactional`, quelli di lettura `@Transactional(readOnly = true)`.
  - `UtenteDAO`: verifica unicità email (`ResourceConflictException`) ed effettua l'hashing della password con `PasswordEncoder` in `save` e `update`.
  - `StanzaDAO`: verifica unicità nome stanza (`ResourceConflictException`), gestisce capienza/descrizione, filtro per capienza minima e ricerca stanze disponibili (`findAvailableRooms`).
  - `PrenotazioneDAO`: valida che `dataI` sia strettamente antecedente a `dataF` (`BadRequestException`), verifica l'esistenza di utente/stanza (`ResourceNotFoundException`), che la stanza sia libera (`ResourceConflictException`) e filtra per mese, anno/mese, stanza o utente.
- **`controller/`**:
  - `UtenteCtrl` (`/api/utenti`): `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`.
  - `StanzaCtrl` (`/api/stanze`): `POST`, `GET` (con `?capienzaMinima=`), `GET /disponibili` (`?dataI=&dataF=&capienzaMinima=`), `GET /{id}`, `PUT /{id}`, `DELETE /{id}`.
  - `PrenotazioneCtrl` (`/api/prenotazioni`): `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`, `GET /timeline/{mese}`, `GET /timeline/{anno}/{mese}`, `GET /stanza/{stanzaId}`, `GET /utente/{utenteId}`.

---

## 3. Ultimi Interventi Effettuati
1. **Rimozione `spring-boot-starter-data-rest`** da `pom.xml` per evitare l'esposizione automatica dei repository JPA in parallelo ai controller custom.
2. **Sicurezza e Password**:
   - Abilitato `.httpBasic(Customizer.withDefaults())` e `BCryptPasswordEncoder` in `WebSecConfig`.
   - Cifratura password utenti in `UtenteDAO` e oscuramento in output tramite `@JsonProperty(access = WRITE_ONLY)` in `UtenteDTO`.
3. **Logica Prenotazioni e Timeline**:
   - Aggiunta validazione `dataI.isBefore(dataF)` e validazione mese (`1..12`) in `PrenotazioneDAO`.
   - Corretta query di overlap in `PrenotazioneRepo` (`<` e `>` anziché `<=` e `>=`) per permettere slot consecutivi.
   - Aggiunto endpoint `GET /api/prenotazioni/timeline/{anno}/{mese}`.
4. **Gestione Eccezioni e Vincoli di Unicità**:
   - Introdotto `GlobalExceptionHandler` (`@RestControllerAdvice`) con eccezioni dedicate (`404`, `409`, `400`).
   - Aggiunti vincoli di unicità e controlli preventivi su `Utente.email` e `Stanza.nome`.
5. **Performance JPA e Transazioni**:
   - Aggiunti `@EntityGraph` su `PrenotazioneRepo` e `@Transactional` su tutti i service (`UtenteDAO`, `StanzaDAO`, `PrenotazioneDAO`).
6. **Test**:
   - Aggiunti test di integrazione in `BookingApplicationTests` (autenticazione Basic, protezione password JSON + BCrypt, validazione date e slot adiacenti/sovrapposti, vincoli di unicità `409`, errori `404`/`400`, timeline per anno e mese).

---

## 4. Possibili Evoluzioni Future (Backlog)
- Autenticazione basata sugli utenti salvati a database (`UserDetailsService` custom su `UtenteRepo` con ruoli) o JWT/OAuth2.
