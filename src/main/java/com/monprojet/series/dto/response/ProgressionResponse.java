// ProgressionResponse.java
package com.monprojet.series.dto.response;

public record ProgressionResponse(
        Long serieId,
        String titre,
        long episodesVus,
        long episodesTotal,
        double pourcentage,
        long tempsVisionneMinutes,
        long tempsRestantMinutes,
        StatutProgression statut
) {
    public enum StatutProgression { A_COMMENCER, EN_COURS, TERMINEE }
}