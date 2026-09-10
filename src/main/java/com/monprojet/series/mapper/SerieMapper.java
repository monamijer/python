// SerieMapper.java
// Plain static methods on purpose: four fields to map, MapStruct would be
// ceremony without payoff here (YAGNI).
package com.monprojet.series.mapper;

import com.monprojet.series.dto.request.SerieRequest;
import com.monprojet.series.dto.response.SerieResponse;
import com.monprojet.series.entity.Serie;

public final class SerieMapper {

    private SerieMapper() {}

    public static Serie toEntity(SerieRequest request) {
        return Serie.builder()
                .titre(request.titre())
                .genre(request.genre())
                .description(request.description())
                .anneeSortie(request.anneeSortie())
                .note(request.note())
                .imageUrl(request.imageUrl())
                .build();
    }

    public static SerieResponse toResponse(Serie serie) {
        return new SerieResponse(
                serie.getId(), serie.getTitre(), serie.getGenre(), serie.getDescription(),
                serie.getAnneeSortie(), serie.getNote(), serie.getImageUrl(), serie.getTmdbId()
        );
    }
}