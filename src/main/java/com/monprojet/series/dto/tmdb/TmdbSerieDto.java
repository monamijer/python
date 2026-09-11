// TmdbSerieDto.java — shape shared by search / popular / similar results
package com.monprojet.series.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbSerieDto(
        Long id,
        String name,
        String overview,
        @JsonProperty("first_air_date") String firstAirDate,
        @JsonProperty("vote_average") Double voteAverage,
        @JsonProperty("poster_path") String posterPath
) {}