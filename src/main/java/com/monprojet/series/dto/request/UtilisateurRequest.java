// UtilisateurRequest.java
package com.monprojet.series.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UtilisateurRequest(
        @NotBlank String pseudo,
        @NotBlank @Email String email
) {}