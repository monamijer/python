package com.monprojet.series.dto.response;

import java.util.List;


public record PageResponse<T>(
        List<T> resultat,
        int page,
        int totalPages,
        int totalResultats
) {
}