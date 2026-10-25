package io.github.everlizarraga.c04py01veterinaria.dtos.propietario;

public record PropietarioResponse(
    Long id,
    String nombre,
    String telefono,
    int cantidadMascotas
) {
}
