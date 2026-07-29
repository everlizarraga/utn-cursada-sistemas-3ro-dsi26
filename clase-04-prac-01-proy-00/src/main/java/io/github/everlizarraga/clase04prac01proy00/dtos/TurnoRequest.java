package io.github.everlizarraga.clase04prac01proy00.dtos;

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
