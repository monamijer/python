// SaisonRequest.java
package com.monprojet.series.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaisonRequest(
        @NotNull @Min(1) Integer numero,
        Integer nbEpisodes
) {}