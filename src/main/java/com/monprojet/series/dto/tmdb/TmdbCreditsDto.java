// TmdbCreditsDto.java
package com.monprojet.series.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbCreditsDto(List<TmdbCastMemberDto> cast) {}