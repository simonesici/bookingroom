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
  - `Utente`: `id`, `name`, `email`, `password` (hash BCrypt).
  - `Stanza`: `id`, `nome`.
  - `Prenotazione`: `id`, `dataCreation` (`@CreationTimestamp`), `utente` (`@ManyToOne(LAZY)`), `stanza` (`@ManyToOne(LAZY)`), `dataI` (`LocalDateTime`), `dataF` (`LocalDateTime`).
- **`dto/`**:
  - `UtenteDTO`: validazione `@NotBlank` / `@Email`; campo `password` annotato con `@JsonProperty(access = WRITE_ONLY)` per non esporlo mai nelle risposte JSON.
  - `StanzaDTO`: `id`, `nome` (`@NotBlank`).
  - `PrenotazioneRequestDTO`: `idUtente`, `idStanza`, `dataI`, `dataF` (tutti `@NotNull`).
  - `PrenotazioneResponseDTO`: `id`, `dataCreation`, `UtenteDTO`, `StanzaDTO`, `dataI`, `dataF`.
- **`repository/`**:
  - `UtenteRepo`, `StanzaRepo`: repository JPA standard.
  - `PrenotazioneRepo`:
    - `findAll()`, `findById(Long id)`, `findByMonth(int month)` con `@EntityGraph(attributePaths = {"utente", "stanza"})` per evitare query N+1 e `LazyInitializationException`.
    - `isStanzaOccupied(stanzaId, startDate, endDate, excludeId)`: verifica sovrapposizioni con `p.dataI < :endDate AND p.dataF > :startDate` (consente prenotazioni adiacenti, es. `09:00-10:00` e `10:00-11:00`).
- **`service/`** (`UtenteDAO`, `StanzaDAO`, `PrenotazioneDAO`):
  - Tutti i metodi di scrittura sono `@Transactional`, quelli di lettura `@Transactional(readOnly = true)`.
  - `UtenteDAO`: effettua l'hashing della password con `PasswordEncoder` in `save` e `update`.
  - `PrenotazioneDAO`: valida che `dataI` sia strettamente antecedente a `dataF` (`validateDates`) e verifica che la stanza sia libera tramite `isStanzaOccupied`.
- **`controller/`**:
  - `UtenteCtrl` (`/api/utenti`): `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`.
  - `StanzaCtrl` (`/api/stanze`): `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`.
  - `PrenotazioneCtrl` (`/api/prenotazioni`): `POST`, `GET`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`, `GET /timeline/{mese}`.

---

## 3. Ultimi Interventi Effettuati
1. **Rimozione `spring-boot-starter-data-rest`** da `pom.xml` per evitare l'esposizione automatica dei repository JPA in parallelo ai controller custom.
2. **Sicurezza e Password**:
   - Abilitato `.httpBasic(Customizer.withDefaults())` e `BCryptPasswordEncoder` in `WebSecConfig`.
   - Cifratura password utenti in `UtenteDAO` e oscuramento in output tramite `@JsonProperty(access = WRITE_ONLY)` in `UtenteDTO`.
3. **Logica Prenotazioni**:
   - Aggiunta validazione `dataI.isBefore(dataF)` in `PrenotazioneDAO`.
   - Corretta query di overlap in `PrenotazioneRepo` (`<` e `>` anziché `<=` e `>=`) per permettere slot consecutivi.
4. **Performance JPA e Transazioni**:
   - Aggiunti `@EntityGraph` su `PrenotazioneRepo` e `@Transactional` su tutti i service (`UtenteDAO`, `StanzaDAO`, `PrenotazioneDAO`).
5. **Test**:
   - Aggiunti test di integrazione in `BookingApplicationTests` (autenticazione Basic, protezione password JSON + BCrypt, validazione date e slot adiacenti/sovrapposti).

---

## 4. Possibili Evoluzioni Future (Backlog)
- Gestione centralizzata delle eccezioni con `@RestControllerAdvice` e codici HTTP dedicati (es. `404 Not Found` per utente/stanza inesistenti, `409 Conflict` per stanza occupata, `400 Bad Request` per errori di validazione).
- Autenticazione basata sugli utenti salvati a database (`UserDetailsService` custom su `UtenteRepo` con ruoli) o JWT/OAuth2.
- Filtro timeline per anno oltre che per mese (`/api/prenotazioni/timeline/{anno}/{mese}`).
- Vincolo di unicità su `Utente.email` e `Stanza.nome`.
