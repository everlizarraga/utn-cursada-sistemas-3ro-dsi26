package io.github.everlizarraga.clase04prac01proy00.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PresupuestoRequest(
    String servicio,
    int cantidad,
    @JsonProperty(value = "precio-unitario")
    double precioUnitario
) {
}
