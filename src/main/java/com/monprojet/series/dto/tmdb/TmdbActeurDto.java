// TmdbActeurDto.java — shape returned by /search/person
package com.monprojet.series.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbActeurDto(
        Long id,
        String name,
        @JsonProperty("known_for_department") String departement,
        @JsonProperty("profile_path") String profilePath
) {}