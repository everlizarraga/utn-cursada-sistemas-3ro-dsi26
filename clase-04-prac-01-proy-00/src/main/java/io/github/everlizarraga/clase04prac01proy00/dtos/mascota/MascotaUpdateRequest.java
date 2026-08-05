package io.github.everlizarraga.clase04prac01proy00.dtos.mascota;

public record MascotaUpdateRequest(
    Long propietarioId,
    String nombre,
    String especie
) {
}
