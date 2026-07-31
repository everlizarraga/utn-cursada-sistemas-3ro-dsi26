package io.github.everlizarraga.clase04prac01proy00.dtos.mascota;

public record MascotaResponse(
    Long id,
    Long propietarioId,
    String nombre,
    String especie
) {
}
