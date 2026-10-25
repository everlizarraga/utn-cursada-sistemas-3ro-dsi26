package io.github.everlizarraga.c04py01veterinaria.dtos.mascota;

public record MascotaCreateRequest(
    Long propietarioId,
    String nombre,
    String especie
) {
}
