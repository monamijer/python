// TmdbPageResponse.java — TMDB wraps every list endpoint in { results: [...], page, total_pages, total_results }
package com.monprojet.series.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbPageResponse(
        List<TmdbSerieDto> results,
        int page,
        @JsonProperty("total_pages") int totalPages,
        @JsonProperty("total_results") int totalResults
) {
}