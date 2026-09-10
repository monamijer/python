// SerieRequest.java
package com.monprojet.series.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SerieRequest(
        @NotBlank(message = "Le titre est obligatoire") String titre,
        String genre,
        String description,
        Integer anneeSortie,
        @Min(0) @Max(10) Double note,
        String imageUrl
) {}