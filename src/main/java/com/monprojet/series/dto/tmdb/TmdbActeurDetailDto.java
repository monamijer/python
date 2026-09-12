// TmdbActeurDetailDto.java
package com.monprojet.series.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbActeurDetailDto(
        Long id,
        String name,
        String biography,
        String birthday,
        @JsonProperty("place_of_birth") String lieuNaissance,
        @JsonProperty("profile_path") String profilePath
) {}