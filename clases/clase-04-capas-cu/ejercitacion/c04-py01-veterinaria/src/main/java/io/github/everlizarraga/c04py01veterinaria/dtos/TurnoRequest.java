package io.github.everlizarraga.c04py01veterinaria.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TurnoRequest(
    //@JsonProperty(required = true)
    String mascota,
    String dia,
    //@JsonProperty("duracion-minutos")
    @JsonProperty(value = "duracion-minutos")
    Integer duracionMinutos
) {
  public TurnoRequest {
    if(dia == null) dia = "lunes";
  }
}
