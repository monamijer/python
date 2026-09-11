// AuthController.java
package com.monprojet.series.controller;

import com.monprojet.series.dto.request.ConnexionRequest;
import com.monprojet.series.dto.request.InscriptionRequest;
import com.monprojet.series.dto.response.AuthResponse;
import com.monprojet.series.entity.Utilisateur;
import com.monprojet.series.repository.UtilisateurRepository;
import com.monprojet.series.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/inscription")
    public ResponseEntity<AuthResponse> inscription(@Valid @RequestBody InscriptionRequest request) {
        if (utilisateurRepository.existsByEmail(request.email())) {
            throw new com.monprojet.series.exception.BusinessException("Cet email est déjà utilisé.");
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .pseudo(request.pseudo())
                .email(request.email())
                .motDePasse(passwordEncoder.encode(request.motDePasse()))
                .build(); // role = USER par défaut (voir l'entité)

        utilisateur = utilisateurRepository.save(utilisateur);

        String token = jwtService.genererToken(
                org.springframework.security.core.userdetails.User
                        .withUsername(utilisateur.getEmail())
                        .password(utilisateur.getMotDePasse())
                        .authorities("ROLE_" + utilisateur.getRole().name())
                        .build()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, utilisateur.getId(), utilisateur.getPseudo(), utilisateur.getRole().name()));
    }

    @PostMapping("/connexion")
    public AuthResponse connexion(@Valid @RequestBody ConnexionRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.motDePasse())
        );

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.email())
                .orElseThrow(); // ne peut pas arriver : authenticate() aurait déjà échoué

        String token = jwtService.genererToken(
                (UserDetails) org.springframework.security.core.userdetails.User
                        .withUsername(utilisateur.getEmail())
                        .password(utilisateur.getMotDePasse())
                        .authorities("ROLE_" + utilisateur.getRole().name())
                        .build()
        );

        return new AuthResponse(token, utilisateur.getId(), utilisateur.getPseudo(), utilisateur.getRole().name());
    }
}