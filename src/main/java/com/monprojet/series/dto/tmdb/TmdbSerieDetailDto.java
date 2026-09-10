// TmdbSerieDetailDto.java
package com.monprojet.series.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbSerieDetailDto(
        Long id,
        String name,
        String overview,
        @JsonProperty("first_air_date") String firstAirDate,
        @JsonProperty("vote_average") Double voteAverage,
        @JsonProperty("poster_path") String posterPath,
        List<TmdbSeasonDto> seasons
) {}