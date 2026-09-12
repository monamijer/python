// TmdbGenresWrapper.java
// TMDB wraps the genre list as { "genres": [...] }
package com.monprojet.series.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbGenresWrapper(List<TmdbGenreDto> genres) {}