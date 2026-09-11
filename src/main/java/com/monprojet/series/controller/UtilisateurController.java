package com.monprojet.series.controller;

import com.monprojet.series.dto.request.UtilisateurRequest;
import com.monprojet.series.dto.response.UtilisateurResponse;
import com.monprojet.series.mapper.UtilisateurMapper;
import com.monprojet.series.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    public List<UtilisateurResponse> lister() {
        return utilisateurService.listerTous().stream().map(UtilisateurMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public UtilisateurResponse obtenir(@PathVariable Long id) {
        return UtilisateurMapper.toResponse(utilisateurService.obtenirParId(id));
    }

    // @PostMapping
    // public ResponseEntity<UtilisateurResponse> creer(@Valid @RequestBody UtilisateurRequest request) {
    //     var cree = utilisateurService.creer(UtilisateurMapper.toEntity(request));
    //     return ResponseEntity.status(HttpStatus.CREATED).body(UtilisateurMapper.toResponse(cree));
    // }
}