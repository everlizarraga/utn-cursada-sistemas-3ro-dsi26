package io.github.everlizarraga.clase04prac01proy00.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PresupuestoResponse(
    String servicio,
    int cantidad,
    double precioUnitario,
    double total
) {
}
