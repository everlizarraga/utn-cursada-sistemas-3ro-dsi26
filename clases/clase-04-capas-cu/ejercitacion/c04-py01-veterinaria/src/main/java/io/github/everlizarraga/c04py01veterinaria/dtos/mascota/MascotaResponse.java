package io.github.everlizarraga.c04py01veterinaria.dtos.mascota;

public record MascotaResponse(
    Long id,
    Long propietarioId,
    String nombre,
    String especie
) {
}
