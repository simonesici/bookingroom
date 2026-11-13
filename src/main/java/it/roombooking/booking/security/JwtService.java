package it.roombooking.booking.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.jwt.secret:bookingroom-super-secret-jwt-key-2026-hs256-secure}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-seconds:86400}")
    private long expirationSeconds;

    public String generateToken(UserDetails userDetails) {
        try {
            long now = Instant.now().getEpochSecond();
            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_USER");

            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", userDetails.getUsername());
            payload.put("role", role);
            payload.put("iat", now);
            payload.put("exp", now + expirationSeconds);

            String encodedHeader = URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(header));
            String encodedPayload = URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(payload));
            String signingInput = encodedHeader + "." + encodedPayload;
            String signature = sign(signingInput);

            return signingInput + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("Impossibile generare il token JWT", e);
        }
    }

    public String extractUsername(String token) {
        Map<String, Object> claims = extractAllClaims(token);
        return claims != null ? (String) claims.get("sub") : null;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Map<String, Object> claims = extractAllClaims(token);
        if (claims == null) {
            return false;
        }
        String subject = (String) claims.get("sub");
        Number exp = (Number) claims.get("exp");
        if (subject == null || exp == null) {
            return false;
        }
        long now = Instant.now().getEpochSecond();
        return subject.equalsIgnoreCase(userDetails.getUsername()) && exp.longValue() > now;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private Map<String, Object> extractAllClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String signingInput = parts[0] + "." + parts[1];
            String expectedSig = sign(signingInput);
            if (!MessageDigest.isEqual(
                    expectedSig.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                return null;
            }
            byte[] payloadBytes = URL_DECODER.decode(parts[1]);
            return objectMapper.readValue(payloadBytes, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGO);
        SecretKeySpec keySpec = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return URL_ENCODER.encodeToString(rawHmac);
    }
}
