package it.roombooking.booking.controller;

import it.roombooking.booking.dto.AuthResponseDTO;
import it.roombooking.booking.dto.LoginRequestDTO;
import it.roombooking.booking.model.Utente;
import it.roombooking.booking.repository.UtenteRepo;
import it.roombooking.booking.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthCtrl {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UtenteRepo utenteRepo;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        AuthResponseDTO response = new AuthResponseDTO(
                token,
                jwtService.getExpirationSeconds(),
                userDetails.getUsername(),
                role
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> currentUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("username", userDetails.getUsername());
        profile.put("authorities", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());

        Optional<Utente> dbUser = utenteRepo.findByEmailIgnoreCase(userDetails.getUsername())
                .or(() -> utenteRepo.findByNameIgnoreCase(userDetails.getUsername()));
        dbUser.ifPresent(u -> {
            profile.put("id", u.getId());
            profile.put("name", u.getName());
            profile.put("email", u.getEmail());
            profile.put("role", u.getRole());
        });

        return ResponseEntity.ok(profile);
    }
}
