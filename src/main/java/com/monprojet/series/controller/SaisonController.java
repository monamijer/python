// SaisonController.java
package com.monprojet.series.controller;

import com.monprojet.series.dto.request.SaisonRequest;
import com.monprojet.series.dto.response.SaisonResponse;
import com.monprojet.series.mapper.SaisonMapper;
import com.monprojet.series.service.SaisonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SaisonController {

    private final SaisonService saisonService;

    @GetMapping("/api/series/{serieId}/saisons")
    public List<SaisonResponse> listerParSerie(@PathVariable Long serieId) {
        return saisonService.listerParSerie(serieId).stream().map(SaisonMapper::toResponse).toList();
    }

    @PostMapping("/api/series/{serieId}/saisons")
    public ResponseEntity<SaisonResponse> creer(
            @PathVariable Long serieId, @Valid @RequestBody SaisonRequest request) {
        var creee = saisonService.creer(serieId, SaisonMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(SaisonMapper.toResponse(creee));
    }
}