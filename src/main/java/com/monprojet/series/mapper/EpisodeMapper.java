// EpisodeMapper.java
package com.monprojet.series.mapper;

import com.monprojet.series.dto.request.EpisodeRequest;
import com.monprojet.series.dto.response.EpisodeResponse;
import com.monprojet.series.entity.Episode;

public final class EpisodeMapper {

    private EpisodeMapper() {}

    public static Episode toEntity(EpisodeRequest request) {
        return Episode.builder()
                .numero(request.numero())
                .titre(request.titre())
                .dureeMinutes(request.dureeMinutes())
                .build();
    }

    public static EpisodeResponse toResponse(Episode episode) {
        return new EpisodeResponse(
                episode.getId(), episode.getNumero(), episode.getTitre(),
                episode.getDureeMinutes(), episode.getSaison().getId()
        );
    }
}