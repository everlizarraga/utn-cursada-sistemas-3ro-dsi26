package io.github.everlizarraga.clase04prac01proy00.dtos.propietario;

public record PropietarioResponse(
    Long id,
    String nombre,
    String telefono,
    int cantidadMascotas
) {
}
