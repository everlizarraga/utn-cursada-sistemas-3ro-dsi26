package io.github.everlizarraga.c04py01veterinaria.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PresupuestoRequest(
    String servicio,
    int cantidad,
    @JsonProperty(value = "precio-unitario")
    double precioUnitario
) {
}
