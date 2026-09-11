package com.monprojet.series.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey cleSecrete;
    private final long dureeValiditeMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long dureeValiditeMs
    ) {
        // Ton secret est en Base64 (44 chars, finit par '=') → on décode
        // pour obtenir les 32 bytes prévus par HS256.
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.cleSecrete = Keys.hmacShaKeyFor(keyBytes);
        this.dureeValiditeMs = dureeValiditeMs;
    }

    public String genererToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + dureeValiditeMs))
                .signWith(cleSecrete)
                .compact();
    }

    public String extraireEmail(String token) {
        return Jwts.parser()
                .verifyWith(cleSecrete)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean estValide(String token, UserDetails userDetails) {
        try {
            String email = extraireEmail(token);
            Date expiration = Jwts.parser()
                    .verifyWith(cleSecrete)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return email.equals(userDetails.getUsername()) && expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}