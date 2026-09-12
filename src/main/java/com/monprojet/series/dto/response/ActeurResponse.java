// ActeurResponse.java
package com.monprojet.series.dto.response;

public record ActeurResponse(
        Long id, String nom, String biographie,
        String dateNaissance, String lieuNaissance, String photoUrl
) {}