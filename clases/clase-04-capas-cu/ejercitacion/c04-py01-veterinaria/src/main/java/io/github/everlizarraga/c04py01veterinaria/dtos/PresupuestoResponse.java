package io.github.everlizarraga.c04py01veterinaria.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PresupuestoResponse(
    String servicio,
    int cantidad,
    double precioUnitario,
    double total
) {
}
