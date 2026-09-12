// TmdbActeurSeriesWrapper.java — shape of /person/{id}/tv_credits
package com.monprojet.series.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbActeurSeriesWrapper(List<TmdbSerieDto> cast) {}