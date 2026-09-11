package com.monprojet.series.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UtilisateurDetailsService utilisateurDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String enTete = request.getHeader("Authorization");

        // Pas de header Authorization ou pas de Bearer → on laisse passer.
        // Spring Security décidera si la route est protégée.
        if (enTete == null || !enTete.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = enTete.substring(7);
        String email;
        try {
            email = jwtService.extraireEmail(token);
        } catch (Exception e) {
            // Token expiré / malformé / signature invalide → on ne met rien
            // dans le SecurityContext et on laisse Spring Security répondre 401/403.
            log.debug("JWT invalide : {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = utilisateurDetailsService.loadUserByUsername(email);

                if (jwtService.estValide(token, userDetails)) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // User supprimé, token pointant vers un user inexistant, etc.
                log.debug("Impossible de charger l'utilisateur {} : {}", email, e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}