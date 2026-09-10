// TmdbSerieResponse.java — what our own API actually returns to the client
package com.monprojet.series.dto.response;

public record TmdbSerieResponse(
        Long tmdbId, String titre, String description,
        String dateDiffusion, Double note, String imageUrl
) {}