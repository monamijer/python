// EpisodeResponse.java
package com.monprojet.series.dto.response;

public record EpisodeResponse(Long id, Integer numero, String titre, Integer dureeMinutes, Long saisonId) {}