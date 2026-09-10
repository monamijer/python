// ApiErrorResponse.java
// Uniform error shape returned to the client, whatever the failure.
package com.monprojet.series.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        LocalDateTime horodatage,
        int statut,
        String erreur,
        String message,
        List<String> details
) {
    public ApiErrorResponse(int statut, String erreur, String message) {
        this(LocalDateTime.now(), statut, erreur, message, List.of());
    }

    public ApiErrorResponse(int statut, String erreur, String message, List<String> details) {
        this(LocalDateTime.now(), statut, erreur, message, details);
    }
}