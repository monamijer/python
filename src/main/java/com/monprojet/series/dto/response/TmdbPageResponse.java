// TmdbPageResponse.java — TMDB wraps every list endpoint in { results: [...] }
package com.monprojet.series.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbPageResponse(List<TmdbSerieDto> results) {}