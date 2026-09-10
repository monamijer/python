// SaisonMapper.java
package com.monprojet.series.mapper;

import com.monprojet.series.dto.request.SaisonRequest;
import com.monprojet.series.dto.response.SaisonResponse;
import com.monprojet.series.entity.Saison;

public final class SaisonMapper {

    private SaisonMapper() {}

    public static Saison toEntity(SaisonRequest request) {
        return Saison.builder()
                .numero(request.numero())
                .nbEpisodes(request.nbEpisodes())
                .build();
    }

    public static SaisonResponse toResponse(Saison saison) {
        return new SaisonResponse(
                saison.getId(), saison.getNumero(), saison.getNbEpisodes(), saison.getSerie().getId()
        );
    }
}