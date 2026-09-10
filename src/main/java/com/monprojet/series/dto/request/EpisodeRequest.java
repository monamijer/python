// EpisodeRequest.java
package com.monprojet.series.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EpisodeRequest(
        @NotNull @Min(1) Integer numero,
        String titre,
        @Min(0) Integer dureeMinutes
) {}