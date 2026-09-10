// SerieController.java
package com.monprojet.series.controller;

import com.monprojet.series.dto.request.SerieRequest;
import com.monprojet.series.dto.response.SerieResponse;
import com.monprojet.series.entity.Serie;
import com.monprojet.series.mapper.SerieMapper;
import com.monprojet.series.service.SerieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SerieController {

    private final SerieService serieService;

    @GetMapping
    public List<SerieResponse> lister() {
        return serieService.listerToutes().stream().map(SerieMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public SerieResponse obtenir(@PathVariable Long id) {
        return SerieMapper.toResponse(serieService.obtenirParId(id));
    }

    @PostMapping
    public ResponseEntity<SerieResponse> creer(@Valid @RequestBody SerieRequest request) {
        Serie creee = serieService.creer(SerieMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(SerieMapper.toResponse(creee));
    }

    @PutMapping("/{id}")
    public SerieResponse modifier(@PathVariable Long id, @Valid @RequestBody SerieRequest request) {
        return SerieMapper.toResponse(serieService.modifier(id, SerieMapper.toEntity(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        serieService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}