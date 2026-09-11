// ConnexionRequest.java
package com.monprojet.series.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConnexionRequest(@NotBlank String email, @NotBlank String motDePasse) {}