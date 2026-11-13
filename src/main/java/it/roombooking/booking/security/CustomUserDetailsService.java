package it.roombooking.booking.security;

import it.roombooking.booking.model.Utente;
import it.roombooking.booking.repository.UtenteRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UtenteRepo utenteRepo;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Utente> dbUser = utenteRepo.findByEmailIgnoreCase(username)
                .or(() -> utenteRepo.findByNameIgnoreCase(username));

        if (dbUser.isPresent()) {
            Utente u = dbUser.get();
            String role = (u.getRole() != null && !u.getRole().isBlank()) ? u.getRole().toUpperCase() : "USER";
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            return User.withUsername(u.getEmail())
                    .password(u.getPassword())
                    .roles(role)
                    .build();
        }

        // Fallback to configured bootstrap admin user (e.g. admin / admin)
        SecurityProperties.User configuredUser = securityProperties.getUser();
        if (configuredUser != null && configuredUser.getName() != null
                && configuredUser.getName().equalsIgnoreCase(username)) {
            String[] roles = configuredUser.getRoles().isEmpty()
                    ? new String[]{"ADMIN", "USER"}
                    : configuredUser.getRoles().toArray(new String[0]);
            return User.withUsername(configuredUser.getName())
                    .password(passwordEncoder.encode(configuredUser.getPassword()))
                    .roles(roles)
                    .build();
        }

        throw new UsernameNotFoundException("Utente non trovato: " + username);
    }
}
