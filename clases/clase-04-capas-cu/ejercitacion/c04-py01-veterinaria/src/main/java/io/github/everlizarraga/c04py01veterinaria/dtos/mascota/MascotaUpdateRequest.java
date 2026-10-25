package io.github.everlizarraga.c04py01veterinaria.dtos.mascota;

public record MascotaUpdateRequest(
    Long propietarioId,
    String nombre,
    String especie
) {
}
