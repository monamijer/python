// MarquerVisionnageResponse.java
package com.monprojet.series.dto.response;

// saisonTerminee / prochaineSaisonNumero let the client offer "start next season" (RG2)
public record MarquerVisionnageResponse(
        Long episodeId,
        boolean saisonTerminee,
        Integer prochaineSaisonNumero
) {}